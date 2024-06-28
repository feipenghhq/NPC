/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/24/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Axi4Lite Arbiter
 * ------------------------------------------------------------------------------------------------
 */
package bus.Axi4Lite

import spinal.core._
import spinal.lib._
import config._

/**
 * Axi4LiteArbiter
 * - config: AXI config
 * - count:  Number of host
 * - fixCombLoop: Fix combLoop in multiple cycle CPU
 */
case class Axi4LiteArbiter(config: Axi4LiteConfig, count: Int, fixCombLoop: Boolean = false) extends Component {
    val io = new Bundle {
        val input = Vec(slave(Axi4Lite(config)), count)
        val output = master(Axi4Lite(config))
    }
    noIoPrefix()

    // Use a round robin arbiter for now, the arbitration result can change
    // if the downstream port is not ready
    def _arbiter[T <: Data](inputs: IndexedSeq[Stream[T]], consumed: Bool) = new Area {
        val size = inputs.size
        val grantOH = UInt(size bits)

        val lastGrant = RegNextWhen(grantOH, consumed) init 1 // record that previous grantOH for the
        val req = inputs.map(_.valid).asBits().asUInt
        val doubleReq = req @@ req
        val doubleGrant = doubleReq & (doubleReq - lastGrant.rotateLeft(1).resized)
        grantOH := doubleGrant(0, size bits) | doubleGrant(size, size bits)
        val grant = OHToUInt(grantOH)
    }

    val read = new Area {
        val ar = io.input.map((f: Axi4Lite) => f.ar)
        val r = io.input.map((f: Axi4Lite) => f.r)

        val consumed = io.output.ar.fire
        val arbiter = _arbiter(ar, consumed)

        // store the wining result for route the transaction back
        // the AXI4Lite does not support outstanding transaction so read response
        // must come back before a new request
        val selectedOH = RegNextWhen(arbiter.grantOH, io.output.ar.fire) init 1
        val respSelOH = cloneOf(selectedOH)
        if (fixCombLoop) {
            respSelOH := selectedOH
        } else {
            // This is the correct implementation but create a combo loop in our multiple-cycle CPU design
            respSelOH := io.output.ar.fire ? arbiter.grantOH | selectedOH
        }

        // AR channel
        io.output.ar.valid := ar.map(_.valid).reduceBalancedTree(_ || _)
        io.output.ar.payload := ar.map(_.payload).read(arbiter.grant)
        ar.zipWithIndex.foreach(f => f._1.ready := io.output.ar.ready & arbiter.grantOH(f._2))

        // R channel
        io.output.r.ready := (r.map(_.ready).asBits & respSelOH.asBits).orR
        r.zipWithIndex.foreach(f => f._1.valid := io.output.r.valid & respSelOH(f._2))
        r.foreach(_.payload := io.output.r.payload)
    }

    val write = new Area {
        val aw = io.input.map((f: Axi4Lite) => f.aw)
        val w = io.input.map((f: Axi4Lite) => f.w)
        val b = io.input.map((f: Axi4Lite) => f.b)

        val consumed = io.output.aw.fire
        val arbiter = _arbiter(aw, consumed)

        // store the wining result for route the transaction back
        // the AXI4Lite does not support outstanding transaction so read response
        // must come back before a new request
        val selectedOH = RegNextWhen(arbiter.grantOH, io.output.aw.fire) init 1
        val channelSelOH = io.output.aw.fire ? arbiter.grantOH | selectedOH
        val channelSel = OHToUInt(channelSelOH)

        // AW channel
        io.output.aw.valid := aw.map(_.valid).reduceBalancedTree(_ || _)
        io.output.aw.payload := aw.map(_.payload).read(arbiter.grant)
        aw.zipWithIndex.foreach(f => f._1.ready := io.output.aw.ready & arbiter.grantOH(f._2))

        // W channel
        io.output.w.valid := w.map(_.valid).reduceBalancedTree(_ || _)
        io.output.w.payload := w.map(_.payload).read(channelSel)
        w.zipWithIndex.foreach(f => f._1.ready := io.output.aw.ready & channelSelOH(f._2))

        // B channel
        io.output.b.ready := (b.map(_.ready).asBits & channelSelOH.asBits).orR
        b.zipWithIndex.foreach(f => f._1.valid := io.output.r.valid & channelSelOH(f._2))
        b.foreach(_.payload := io.output.b.payload)
    }

}

object Axi4LiteArbiterVerilog extends App {
    val config = Axi4LiteConfig()
    Config.spinal.generateVerilog(Axi4LiteArbiter(config, 4))
}