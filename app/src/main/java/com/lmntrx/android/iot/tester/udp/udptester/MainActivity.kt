package com.lmntrx.android.iot.tester.udp.udptester

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException


class MainActivity : AppCompatActivity() {

    var ip = ""
    var port = ""
    var message = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        doAsync { ReceiverThread().start() }

        sendButton.setOnClickListener {

            if (ipEditText.text.isNullOrEmpty())
                ipEditText.setText(R.string.sample_ip)

            if (portEditText.text.isNullOrEmpty())
                portEditText.setText(R.string.sample_port)

            if (messageEditText.text.isNullOrEmpty())
                messageEditText.setText(R.string.hello_world)

            ip = ipEditText.text.toString()
            port = portEditText.text.toString()
            message = messageEditText.text.toString()

            doAsync { sendMessage(ip, port, message) }

        }

    }

    private fun sendMessage(ip: String, port: String, message: String) {
        val ia = InetAddress.getByName(ip)
        val sender = SenderThread(ia, port.toInt(), message)
        sender.start()
    }

    inner class SenderThread @Throws(SocketException::class)
    constructor(private val server: InetAddress, private val port: Int, private val message: String) : Thread() {

        private val socket: DatagramSocket = DatagramSocket()

        init {
            this.socket.connect(server, port)
        }

        override fun run() {

            try {
                val data = message.toByteArray()
                val output = DatagramPacket(data, data.size, server, port)
                socket.send(output)
                runOnUiThread { toast("Sent $message") }
                Thread.yield()
            } catch (ex: IOException) {
                System.err.println(ex)
            }

        }
    }

    inner class ReceiverThread @Throws(SocketException::class)
    constructor() : Thread() {
        private var socket: DatagramSocket = DatagramSocket()

        init {
            // UDP Server is run on device's 3001 port
            socket.connect(InetAddress.getByName("localhost"),3001)
        }

        override fun run() {


            try {
                val port = 11000

                val dsocket = DatagramSocket(port)
                val buffer = ByteArray(2048)
                val packet = DatagramPacket(buffer, buffer.size)

                while (true) {

                    dsocket.receive(packet)
                    val message = String(buffer, 0, packet.length)
                    runOnUiThread {
                        messageEditText.setText(message)
                        doAsync {
                            sendButton.callOnClick()
                        }
                        toast("Message received")
                    }

                    packet.length = buffer.size
                }
            } catch (e: Exception) {
                System.err.println(e)
                e.printStackTrace()
            }

        }
    }
}

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

