"""Deploy AIBase JARs and frontend to server 10.139.11.100."""
import paramiko
import os

SERVER = "10.139.11.100"
USER = "root"
PASSWORD = "Dt1q2w3e4r!"
REMOTE_BASE = "/u01/aibase"

# Module -> docker-compose service name (app.yml)
MODULES = {
    "ai-base-knowledge":      "knowledge",
    "ai-base-workflow":       "workflow",
    "ai-base-skill":          "skill",
    "ai-base-agent":          "agent",
    "ai-base-mcp-gateway":    "mcp-gateway",
    "ai-base-model-gateway":  "model-gateway",
    "ai-base-eval":           "eval",
    "ai-base-platform":       "platform",
    "ai-base-gateway":        "api-gateway",
}

BASE_DIR = os.path.dirname(os.path.abspath(__file__))


def run_cmd(ssh, cmd):
    """Run remote command, print exit code and stderr if failed."""
    stdin, stdout, stderr = ssh.exec_command(cmd)
    ec = stdout.channel.recv_exit_status()
    err = stderr.read().decode().strip()
    if ec != 0 and err:
        print(f"    [{ec}] {err[:300]}")
    return ec


def main():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    print(f"Connecting to {SERVER}...")
    ssh.connect(SERVER, username=USER, password=PASSWORD)
    print("Connected!\n")

    sftp = ssh.open_sftp()

    # === Step 1: Upload JARs ===
    print("--- Uploading JARs ---")
    for module_name, svc_name in MODULES.items():
        jar_filename = f"{module_name}-1.0.0-SNAPSHOT.jar"
        local = os.path.join(BASE_DIR, module_name, "target", jar_filename)
        if not os.path.exists(local):
            print(f"  SKIP {module_name} ({svc_name}): JAR not found")
            continue

        # Ensure remote target/ dir
        remote_tgt = f"{REMOTE_BASE}/{module_name}/target"
        run_cmd(ssh, f"mkdir -p {remote_tgt}")

        remote = f"{remote_tgt}/{jar_filename}"
        size_mb = os.path.getsize(local) / 1024 / 1024
        print(f"  {module_name} ({size_mb:.1f} MB) -> {svc_name}", end=" ")
        sftp.put(local, remote)
        print("OK")

    # === Step 2: Upload Frontend ===
    print("\n--- Uploading Frontend ---")
    dist_dir = os.path.join(BASE_DIR, "ai-base-frontend", "dist")
    nginx_conf = os.path.join(BASE_DIR, "ai-base-frontend", "nginx.conf")
    remote_fe = f"{REMOTE_BASE}/ai-base-frontend"

    if os.path.exists(dist_dir):
        run_cmd(ssh, f"rm -rf {remote_fe}/dist")
        run_cmd(ssh, f"mkdir -p {remote_fe}/dist")
        count = 0
        for root, dirs, files in os.walk(dist_dir):
            for fname in files:
                local_path = os.path.join(root, fname)
                rel = os.path.relpath(local_path, dist_dir).replace("\\", "/")
                rpath = f"{remote_fe}/dist/{rel}"
                rdir = os.path.dirname(rpath)
                try:
                    sftp.stat(rdir)
                except FileNotFoundError:
                    run_cmd(ssh, f"mkdir -p {rdir}")
                sftp.put(local_path, rpath)
                count += 1
        print(f"  dist: {count} files uploaded")
    else:
        print("  WARN: dist/ not found")

    if os.path.exists(nginx_conf):
        sftp.put(nginx_conf, f"{remote_fe}/nginx.conf")
        print("  nginx.conf: OK")

    sftp.close()

    # === Step 3: Rebuild Docker images ===
    print("\n--- Rebuilding Docker Images ---")
    compose_dir = f"cd {REMOTE_BASE}"
    svc_list = list(MODULES.values())

    for svc in svc_list:
        print(f"  Building {svc}...", end=" ")
        ec = run_cmd(ssh, f"{compose_dir} && docker-compose build {svc} 2>&1")
        print("OK" if ec == 0 else f"ERR({ec})")

    # === Step 4: Restart containers ===
    print("\n--- Restarting Containers ---")
    for svc in svc_list:
        print(f"  Restarting {svc}...", end=" ")
        ec = run_cmd(ssh, f"{compose_dir} && docker-compose up -d --no-deps {svc} 2>&1")
        print("OK" if ec == 0 else f"ERR({ec})")

    # === Step 5: Restart frontend ===
    print("\n--- Restarting Frontend ---")
    ec = run_cmd(ssh, f"{compose_dir} && docker-compose up -d --no-deps frontend 2>&1")
    print(f"  frontend: {'OK' if ec == 0 else f'ERR({ec})'}")

    # === Step 6: Show status ===
    print("\n--- Container Status ---")
    stdin, stdout, stderr = ssh.exec_command(
        "docker ps --format 'table {{.Names}}\t{{.Status}}' --filter name=aibase | head -15"
    )
    print(stdout.read().decode())

    ssh.close()
    print("=== Deployment Complete ===")


if __name__ == "__main__":
    main()
