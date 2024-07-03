/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/23/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Axi4Lite Decoder
 * ------------------------------------------------------------------------------------------------
 */
package bus.Axi4Lite

import spinal.core._
import spinal.lib._
import config._

case class Axi4LiteDecoder(config: Axi4LiteConfig, decoding: Seq[Range]) extends Component {
    val io = new Bundle {
        val input = slave(Axi4Lite(config))
        val output = Vec(master(Axi4Lite(config)), decoding.size)
    }
    noIoPrefix()

    val read = new Area {
        // Decode the AR channel
        val araddr = io.input.ar.payload.araddr
        val hits = decoding.map(r => (araddr >= r.start && araddr <= r.end))
        val hitsBuffer = RegNextWhen(hits.asBits, io.input.ar.valid) init 0
        for ((output, hit) <- io.output.zip(hits)) {
            output.ar.valid := io.input.ar.valid & hit
            output.ar.payload <> io.input.ar.payload
        }
        io.input.ar.ready := (io.output.map(_.ar.ready).asBits & hits.asBits).orR

        // Route back the R channel
        val respHits = io.input.ar.valid ? hits.asBits | hitsBuffer
        io.output.foreach(ch => ch.r.ready := io.input.r.ready)
        io.input.r.valid := (io.output.map(_.r.valid).asBits & respHits).orR
        io.input.r.payload <> MuxOH(respHits, io.output.map(_.r.payload))
    }

    val write = new Area {
        // Decode the AW channel
        val awaddr = io.input.aw.payload.awaddr
        val hits = decoding.map(r => (awaddr >= r.start && awaddr <= r.end))
        val hitsBuffer = RegNextWhen(hits.asBits, io.input.aw.valid) init 0
        for ((output, hit) <- io.output.zip(hits)) {
            output.aw.valid := io.input.aw.valid & hit
            output.aw.payload <> io.input.aw.payload
        }
        io.input.aw.ready := (io.output.map(_.ar.ready).asBits & hits.asBits).orR

        // Route the W channel
        val hitsFinal = io.input.aw.valid ? hits.asBits | hitsBuffer
        for ((output, hit) <- io.output.zip(hitsFinal.subdivideIn(1 bits))) {
            output.w.valid := io.input.w.valid & hit.asBool
            output.w.payload <> io.input.w.payload
        }
        io.input.w.ready := (io.output.map(_.w.ready).asBits & hitsFinal).orR

        // Route back the B channel
        io.output.foreach(ch => ch.b.ready := io.input.b.ready)
        io.input.b.valid := (io.output.map(_.b.valid).asBits & hitsFinal).orR
        io.input.b.payload <> MuxOH(hitsFinal, io.output.map(_.b.payload))
    }
}

object Axi4LiteDecoderVerilog extends App {
    val config = Axi4LiteConfig()
    val decoding = Seq(
        Range(0x0, 0x0FFFFFFF),
        Range(0x10000000, 0x1FFFFFFF),
        Range(0x20000000, 0x2FFFFFFF),
        Range(0x30000000, 0x3FFFFFFF),
    )
    Config.spinal.generateVerilog(Axi4LiteDecoder(config, decoding))
}