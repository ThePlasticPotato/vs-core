# Valkyrien Skies Networking System

Valkyrien Skies has two main networking systems - the basic networking `Packet`
that
uses and operates with raw Netty `ByteBuf`s, and the `SimplePacket` system
implemented on top of that basic system that makes it easier to use.

## `SimplePacket`

To use `SimplePacket`, first create your own class implementing it:

```kt
data class MyPacket(
    val field1: String,
    val field2: Int
) : SimplePacket
```

Then register it in `Packets` (or anywhere, just make sure all packets are
registered in the same order on the client and server)

```kt
init {
    MyPacket::class.register()
}
```

You can also directly implement handlers in this class if you wish:

```kt
data class MyPacket(
    val field1: String,
    val field2: Int
) : SimplePacket {
    override fun receivedByClient() {}

    override fun receivedByServer(player: IPlayer) {}
}
```

To register a handler, simply do:

```kt
MyPacket::class.registerServerHandler { packet, player ->
    // do stuff
}
```

> Remember: the handler doesn't run on the game thread.

## `UDP` Protocol

If a player joins it will use the TCP by default, and on the clients request
by sending `PacketRequestUdp` it will request a UDP 'connection'.

The server will return a `PacketUdpState` it contains the port of the UDP
and a unique Long that will be used to identify the UDP 'connection'.

To Initialize the UDP connection the client sends a 8-byte packet with the
unique Long, and then the Server shall recognize the connection.

For a final confirmation the server will send the players UUID to them,
so does the client know that everything went OK.



