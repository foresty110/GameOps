#!/usr/bin/env bash
# PostToolUse hook for Edit/Write/MultiEdit
# 파일 수정 후 자동 포맷을 실행한다.
# 실패해도 차단하지 않는다 (exit 0 유지). 포맷 실패는 경고로만.

set -uo pipefail

INPUT="$(cat)"
FILE_PATH="$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')"

if [[ -z "$FILE_PATH" ]] || [[ ! -f "$FILE_PATH" ]]; then
  exit 0
fi

# 프로젝트 루트 찾기 (gradlew나 package.json 기준)
find_project_root() {
  local dir="$(dirname "$FILE_PATH")"
  while [[ "$dir" != "/" ]]; do
    if [[ -f "$dir/gradlew" ]] || [[ -f "$dir/package.json" ]] || [[ -d "$dir/.git" ]]; then
      echo "$dir"
      return 0
    fi
    dir="$(dirname "$dir")"
  done
  return 1
}

case "$FILE_PATH" in
  # 백엔드 Java 파일 → spotless로 포맷
  */backend/*.java)
    BACKEND_DIR="$(find_project_root)/backend"
    if [[ -f "$BACKEND_DIR/gradlew" ]]; then
      # 단일 파일만 빠르게 포맷 (spotlessApply 전체는 느림)
      # spotless가 설정되어 있다고 가정. 없으면 google-java-format 직접 호출.
      if command -v google-java-format &>/dev/null; then
        google-java-format --replace "$FILE_PATH" 2>/dev/null || true
      fi
    fi
    ;;

  # 프론트엔드 TS/TSX/JS/JSX → prettier
  */frontend/*.ts | */frontend/*.tsx | */frontend/*.js | */frontend/*.jsx)
    FRONTEND_DIR="$(find_project_root)/frontend"
    if [[ -f "$FRONTEND_DIR/package.json" ]]; then
      cd "$FRONTEND_DIR"
      if command -v pnpm &>/dev/null && pnpm list prettier &>/dev/null; then
        pnpm exec prettier --write "$FILE_PATH" 2>/dev/null || true
      elif command -v npx &>/dev/null; then
        npx prettier --write "$FILE_PATH" 2>/dev/null || true
      fi
    fi
    ;;

  # JSON, YAML, Markdown → prettier (있으면)
  *.json | *.yaml | *.yml | *.md)
    if command -v prettier &>/dev/null; then
      prettier --write "$FILE_PATH" 2>/dev/null || true
    fi
    ;;
esac

exit 0