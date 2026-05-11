#!/usr/bin/env bash
# PostToolUse hook for Write/Edit
# Spring Controller 파일에서 @PreAuthorize 누락을 감지하고 경고한다.
# 차단(exit 2)하지 않고 경고만 (exit 0). 사람이 판단하도록.

set -uo pipefail

INPUT="$(cat)"
FILE_PATH="$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')"

if [[ -z "$FILE_PATH" ]] || [[ ! -f "$FILE_PATH" ]]; then
  exit 0
fi

# Controller 파일만 검사
case "$FILE_PATH" in
  */backend/*Controller.java) ;;
  *) exit 0 ;;
esac

# 쓰기 메서드(PostMapping, PutMapping, DeleteMapping, PatchMapping) 검출
# @PreAuthorize가 같은 메서드 위에 있는지 단순 검사
WARNINGS=()

# awk로 메서드 단위 분석
while IFS= read -r line_num; do
  # 해당 라인 앞 5줄에 @PreAuthorize가 있는지 확인
  start=$((line_num - 5))
  [[ $start -lt 1 ]] && start=1
  context="$(sed -n "${start},${line_num}p" "$FILE_PATH")"

  if ! echo "$context" | grep -qE '@PreAuthorize|@Secured'; then
    method_line="$(sed -n "${line_num}p" "$FILE_PATH")"
    WARNINGS+=("L${line_num}: $method_line")
  fi
done < <(grep -nE '@(PostMapping|PutMapping|DeleteMapping|PatchMapping)' "$FILE_PATH" | cut -d: -f1)

if [[ ${#WARNINGS[@]} -gt 0 ]]; then
  cat >&2 <<EOF
⚠️  권한 어노테이션 누락 의심:
파일: $FILE_PATH

다음 쓰기 엔드포인트 위 5줄 이내에 @PreAuthorize가 없습니다:
EOF
  for w in "${WARNINGS[@]}"; do
    echo "  - $w" >&2
  done
  cat >&2 <<EOF

어드민 API는 반드시 @PreAuthorize로 권한을 명시해야 합니다.
조회 API(@GetMapping)도 민감 데이터라면 권한이 필요할 수 있습니다.
EOF
fi

exit 0