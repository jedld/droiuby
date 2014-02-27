jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ~/my-release-key.keystore ~/workspace/droiuby/bin/droiuby-release-unsigned.apk droiuby
rm droiuby.apk
zipalign -v 4 ~/workspace/droiuby/bin/droiuby-release-unsigned.apk droiuby.apk



Tigiding tigidong!

