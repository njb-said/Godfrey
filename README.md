# Godfrey
A butler, for linux servers

## Wiki
Help, setup and technical description available on [the wiki](https://github.com/njb-said/Godfrey/wiki).

## Limitations
Godfrey is not ideal if you want to run a command that asks for user input - like mysql server install. If you want to use ```yum``` or ```apt-get``` you can, but you should add the ```-y``` flag (or another workaround) to signify yes to prompts.