# KittySnap
_Forwarding message between Napcat and Minecraft!_  
KittySnap is a plugin that bridges QQ Group messaging and in-game chatting.  

## How to use
1. Download the complied file from actions or release
   - If none of them exists, consider building it yourself
2. Threw the plugin into your server's /plugin folder
3. Start up a [NapcatQQ](https://github.com/NapNeko/NapCatQQ) server and add a `Websocket Server` property
   - For detailed tutorials, see [NapcatQQ official website](https://napneko.github.io/)
4. Switch the folder to /plugin/KittySnap and open `config.yml`
5. Fill out the `WsUrl`, `token`, and `groups` keys
6. Run `/ks reload`
- Ensure that your bot was in your targeted listening group


## Building
You need JDK 21 and a good Internet connection.  
Run the command line below to clone this repository first:
```text
git clone https://github.com/Ph0sphorW/KittySnap/
```
- Switch the folder to the cloned repository folder and start a terminal
- Run command:
```text
.\gradlew build
```
- Get the artifact under /target folder

## Pull Requests
~~What's that?~~

## License
TODO

## Thanks
@XIAYM-gh: Reorganized file structure  
@HaHaWTH: Refactored image handling part