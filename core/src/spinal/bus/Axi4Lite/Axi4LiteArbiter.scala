/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/24/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Axi4Lite Arbiter
 * ------------------------------------------------------------------------------------------------
 */
package bus.Axi4Lite

import common._
import spinal.core._
import spinal.lib._
import config._

/**
 * AXI lite Arbiter
 * @param config AXI config
 * @param count  Number of host
 * @param readZeroDelay read response can be at the same cycle as read request
 */
case class Axi4LiteArbiter(config: Axi4LiteConfig, count: Int, readZeroDelay: Boolean = true) extends Component {
    val io = new Bundle {
        val input = Vec(slave(Axi4Lite(config)), count)
        val output = master(Axi4Lite(config))
    }
    noIoPrefix()

    val read = new Area {
        val ar = io.input.map((f: Axi4Lite) => f.ar)
        val r = io.input.map((f: Axi4Lite) => f.r)

        val arbiter = RrArbiter(ar.length)
        arbiter.io.req <> ar.map(_.valid).asBits
        arbiter.io.enable := io.output.ar.fire

        // The AXI4Lite does not support outstanding transaction so read response
        // must come back before a new request
        val granted = cloneOf(arbiter.io.grant)

        if (readZeroDelay) {
            // This is the correct implementation
            granted := io.output.ar.fire ? arbiter.io.grant | arbiter.io.prevGrant
        } else {
            // This is used to fix an combo loop issue in the multi-cycle CPU design or similar situation where the
            // read response (rsp0) is used in the downstream logic to generate an AXI request (req1) going into the
            // same AXI arbiter with the request (req0)
            // !!! It ASSUMES that the read response come at least ONE cycle after the request.
            granted := arbiter.io.prevGrant
        }

        // AR channel
        ar.zipWithIndex.foreach(f => f._1.ready := io.output.ar.ready & arbiter.io.grant(f._2))
        io.output.ar.valid := ar.map(_.valid).reduceBalancedTree(_ || _)
        io.output.ar.payload := ar.map(_.payload).read(arbiter.io.grantId)

        // R channel
        io.output.r.ready := (r.map(_.ready).asBits & granted.asBits).orR
        r.zipWithIndex.foreach(f => f._1.valid := io.output.r.valid & granted(f._2))
        r.foreach(_.payload := io.output.r.payload)
    }


    val write = new Area {
        val aw = io.input.map((f: Axi4Lite) => f.aw)
        val w = io.input.map((f: Axi4Lite) => f.w)
        val b = io.input.map((f: Axi4Lite) => f.b)

        val arbiter = RrArbiter(aw.length)
        arbiter.io.req <> aw.map(_.valid).asBits
        arbiter.io.enable := io.output.aw.fire

        // The AXI4Lite does not support outstanding transaction so read response
        // must come back before a new request
        val granted = io.output.aw.fire ? arbiter.io.grant | arbiter.io.prevGrant
        val grantedId = OHToUInt(granted)

        // AW channel
        aw.zipWithIndex.foreach(f => f._1.ready := io.output.aw.ready & arbiter.io.grant(f._2))
        io.output.aw.valid := aw.map(_.valid).reduceBalancedTree(_ || _)
        io.output.aw.payload := aw.map(_.payload).read(arbiter.io.grantId)

        // W channel
        w.zipWithIndex.foreach(f => f._1.ready := io.output.w.ready & granted(f._2))
        io.output.w.valid := w.map(_.valid).reduceBalancedTree(_ || _)
        io.output.w.payload := w.map(_.payload).read(grantedId)

        // B channel
        io.output.b.ready := (b.map(_.ready).asBits & granted.asBits).orR
        b.zipWithIndex.foreach(f => f._1.valid := io.output.r.valid & granted(f._2))
        b.foreach(_.payload := io.output.b.payload)
    }
}

object Axi4LiteArbiterVerilog extends App {
    val config = Axi4LiteConfig()
    Config.spinal.generateVerilog(Axi4LiteArbiter(config, 4))
}