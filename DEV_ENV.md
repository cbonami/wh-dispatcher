# WSL2 based dev environment

## optional: install Fluent Terminal

Makes it easy to work with WSL and Powershell from a single terminal.

## install WSL2

## generate SSH key and upload it to github

Needed to access private repo's.

## clone github repo

## Install Docker Desktop

Install [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop).

Right-click on the Docker task bar item, select Settings / Preferences and update Resources > File Sharing with any locations your source code is kept.

Enable the Windows WSL 2 back-end: Right-click on the Docker taskbar item and select Settings. Check Use the WSL 2 based engine and verify your distribution is enabled under Resources > WSL Integration.

## Optional: access Ubuntu filesystem Ubuntu in W10

In W10 :
```
cd \\wsl$\Ubuntu-20.04\home\bonami\IdeaProjects\wh-dispatcher
```

So you could use IntelliJ to edit the code. 
But the problem is using Windows Docker for WSL2.

## Install 'sudo for windows'

[sudo for windows](http://blog.lukesampson.com/sudo-for-windows)

## Install Visual Studio (vscode) on w10

Download [here](https://code.visualstudio.com/download) or use chocolatey:
```
sudo choco install vscode
```
Run it.

## Install Remote Development Extension pack

> Note: check [vscode documentation on remote development](https://code.visualstudio.com/docs/remote) to get the full story

Install the [Remote Development extension pack](https://aka.ms/vscode-remote/download/extension). This will allow you to develop on code located on a 'remote' machine, whether it be the Ubuntu on the WSL2 subsystem, or a dev container running in Docker -- see next 2 options.

## Option 1: develop on Ubuntu machine

### Install vscode-server on Ubuntu

In Ubuntu, install code-server and run it:

```
curl -fsSL https://code-server.dev/install.sh | sh
code-server
```

Long story [here](https://github.com/cdr/code-server).

### Edit code on Ubuntu's filesystem

Run vscode and click the green '><' button in the bottom-left corner of the IDE.

![](./img/remote-wsl-plugin.png)

Select 'Remote-WSL: New window using distro...' from the dropbox, and pick the Ubuntu-20.04 distro.
Open folder and browse to the folder (on the Ubuntu fs) that contains the code.
Start coding :)

> Note: when you open a terminal in vscode, it will be a Ubuntu-bash terminal. Here you can run maven etc, and whatever you install in Ubuntu. The Ubuntu subsystem has become your full-time dev environment.

## Option 2: use a dev container
