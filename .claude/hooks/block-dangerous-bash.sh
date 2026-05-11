#!/usr/bin/env bash
# PreToolUse hook for Bash tool
# 위험한 명령을 차단한다. exit 2로 종료하면 Claude에게 차단 사실이 전달된다.
#
# Claude Code는 hook 입력을 stdin으로 JSON 형태로 전달한다.
# 예: {"tool_input": {"command": "rm -rf /"}}

set -euo pipefail

# stdin에서 JSON 읽기
INPUT="$(cat)"
COMMAND="$(echo "$INPUT" | jq -r '.tool_input.command // empty')"

if [[ -z "$COMMAND" ]]; then
  exit 0
fi

# 차단할 패턴 (정규식)
BLOCK_PATTERNS=(
  # 시스템 파괴
  '^[[:space:]]*rm[[:space:]]+(-[a-zA-Z]*r[a-zA-Z]*[fF]?|-[a-zA-Z]*[fF][a-zA-Z]*r?)[[:space:]]+/'
  '^[[:space:]]*rm[[:space:]]+(-[a-zA-Z]*r[a-zA-Z]*[fF]?|-[a-zA-Z]*[fF][a-zA-Z]*r?)[[:space:]]+~'
  ':[[:space:]]*\(\)[[:space:]]*\{.*\|:'   # fork bomb

  # 비밀 파일 노출
  'cat[[:space:]]+.*\.env'
  'cat[[:space:]]+.*application-prod'
  'cat[[:space:]]+.*\.pem'
  'cat[[:space:]]+.*id_rsa'

  # 위험한 git
  'git[[:space:]]+push[[:space:]]+.*--force'
  'git[[:space:]]+push[[:space:]]+.*-f([[:space:]]|$)'
  'git[[:space:]]+reset[[:space:]]+--hard[[:space:]]+(HEAD~|origin)'

  # DB 통째 날리기
  'DROP[[:space:]]+DATABASE'
  'TRUNCATE[[:space:]]+TABLE'

  # 외부로 키 유출 가능성
  'curl[[:space:]]+.*-d[[:space:]]+.*\$\{?(API_KEY|SECRET|TOKEN|PASSWORD)'
)

for pattern in "${BLOCK_PATTERNS[@]}"; do
  if echo "$COMMAND" | grep -qE "$pattern"; then
    cat >&2 <<EOF
🚨 차단된 명령입니다.
명령: $COMMAND
사유: 위험 패턴 매칭 ($pattern)

이 명령은 데이터 손실, 비밀 정보 노출, 또는 비가역적 변경을 일으킬 수 있습니다.
정말 필요하다면 터미널에서 사용자가 직접 실행해야 합니다.
EOF
    exit 2
  fi
done

exit 0