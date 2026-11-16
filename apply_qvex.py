from pathlib import Path
import shutil

OLD = "com.movtery.zalithlauncher"
NEW = "com.myqvexzz.qvexlauncher"
OLD_JNI = "com_movtery_zalithlauncher"
NEW_JNI = "com_myqvexzz_qvexlauncher"

moves = []
for root in [Path("QVEXLauncher/src"), Path("LWJGL/patches/src")]:
    for part in ["main/java", "test/java", "test/resources"]:
        src = root / part / "com/movtery/zalithlauncher"
        if src.is_dir():
            dst = root / part / "com/myqvexzz/qvexlauncher"
            moves.append((src, dst))

for src, dst in moves:
    if dst.exists():
        raise SystemExit(f"STOP: destination exists: {dst}")

skip = {".git", ".gradle", "build", ".idea"}
changed = 0
for p in Path(".").rglob("*"):
    if not p.is_file() or any(x in skip for x in p.parts):
        continue
    if p.name in {"apply_qvex.py", "migrate_qvex.py", "qvex_package_audit.txt"}:
        continue
    try:
        s = p.read_text(encoding="utf-8")
    except (UnicodeDecodeError, OSError):
        continue
    t = s.replace(OLD, NEW).replace(OLD_JNI, NEW_JNI)
    if t != s:
        p.write_text(t, encoding="utf-8")
        changed += 1

for src, dst in moves:
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.move(str(src), str(dst))

print("MIGRATION COMPLETE")
print("Files updated:", changed)
print("Directories moved:", len(moves))
