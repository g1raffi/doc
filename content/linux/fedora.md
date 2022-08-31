---
weight: 31
title: Fedora

---

## Switch to fedora

Docker replaced with podman:

Non-root problematic with podman and all the other docker-like resources are fixed by setting:


```bash
# Install the required podman packages from dnf. If you're not using rpm based
# distro, replace with respective package manager
sudo dnf install podman podman-docker
# Enable the podman socket with Docker REST API
systemctl --user enable podman.socket --now
# Set the required envvars
export DOCKER_HOST=unix:///run/user/${UID}/podman/podman.sock
export TESTCONTAINERS_RYUK_DISABLED=true
```
