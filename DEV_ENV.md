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

Here, you use the Ubuntu host as the main development platform and run-time for your tests etc.

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

At this point you should already have all needed vscode plugins etc installed. If not, check [these instructions](https://code.visualstudio.com/docs/remote/containers-tutorial).

There's just too much to tell, so have a look at the [complete guide](https://code.visualstudio.com/docs/remote/containers#_quick-start-try-a-development-container)

In short:
* clone this github repo (the one you're looking at)
* open the code in vscode
* click '<>' (or press F1) and pick 'reopen in container'
* wait for the dev container to build

The ![devcontainer.json](./.devcontainer/devcontainer.json) and [Dockerfile](./.devcontainer/Dockerfile) is where the magic happens.

Open a Terminal in vscode:

```
vscode ➜ /workspaces/wh-dispatcher (master ✗) $ mvn --version
Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
Maven home: /usr/local/sdkman/candidates/maven/current
Java version: 11.0.9.1, vendor: Oracle Corporation, runtime: /usr/local/openjdk-11
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "4.19.128-microsoft-standard", arch: "amd64", family: "unix"
```

You're developing in a dev container with all tools (mvn etc) pre-installed. Same tools and versions for all developers = standardization of the dev environment.

## Install vscode-lombok plugin

https://marketplace.visualstudio.com/items?itemName=GabrielBB.vscode-lombok