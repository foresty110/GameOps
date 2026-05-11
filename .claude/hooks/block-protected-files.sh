#!/usr/bin/env bash
# PreToolUse hook for Edit/Write/MultiEdit tools
# 비밀 파일이나 운영 환경 파일 수정을 차단한다.

set -euo pipefail

INPUT="$(cat)"
FILE_PATH="$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')"

if [[ -z "$FILE_PATH" ]]; then
  exit 0
fi

# 보호 대상 파일 패턴
PROTECTED_PATTERNS=(
  '\.env$'
  '\.env\.[^.]+$'              # .env.production 등
  'application-prod\.ya?ml$'
  '/secrets/'
  'id_rsa($|\.)'
  '\.pem$'
  '\.key$'
  '/\.git/config$'              # git 설정 직접 수정 금지
  '/credentials\.json$'
)

for pattern in "${PROTECTED_PATTERNS[@]}"; do
  if echo "$FILE_PATH" | grep -qE "$pattern"; then
    cat >&2 <<EOF
🚨 보호된 파일 수정이 차단되었습니다.
경로: $FILE_PATH
사유: 비밀 정보 또는 운영 환경 파일

수정이 정말 필요하다면 사용자가 직접 편집기로 수정해야 합니다.
예시 파일(.env.example)이 필요한 경우라면 .env.example을 따로 만드세요.
EOF
    exit 2
  fi
done

exit 0