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
 * Note:
 * 1. Currently the arbiter does not support outstanding transaction so response must come back
 *    before a new request. A new request will be blocked if the response has not came back.
 * 2. There is a possibility that different requests from different channels might come together,
 *    For example, read from ch1 and write from ch2 comes together. In theory, the read and write
 *    request are parallel and the arbiter should be able to handle this case.
 *    However, the slave need to be able to support both read and write command arrives at the
 *    same time since they might be targeting the same device.
 * 3. We need to synchronize AW and W channel because we need to do the arbitration for different
 *    requests. Only when both the AW and W channel request arrives we will consider it as an candidate
 *    for the arbitration.
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
 * @param parallelRW true -  Support simultaneous read and write request meaning arvalid and awvalid/wvalid can assert
 *                           at the same time from either same or different requester.
 *                           This is compliant to AXI lite specification and should be use for IP that are fully AXI
 *                           lite compliant
 *                   false - Not support simultaneous read and write request meaning arvalid and awvalid/wavlid can't
 *                           assert at the same time from same requester. If there are read and write requests from
 *                           different requesters at the same time, then only one req from one requester will be
 *                           processed.
 *                           This is NOT compliant to AXI lite spec and should only be used when the downstream
 *                           device does not support simultaneous read and write.
 *                           This option is added because I have such IP and use case in my project.
 *
 */
case class Axi4LiteArbiter(config: Axi4LiteConfig, count: Int, parallelRW: Boolean = true) extends Component {
    val io = new Bundle {
        val input = Vec(slave(Axi4Lite(config)), count)
        val output = master(Axi4Lite(config))
    }
    noIoPrefix()

    // channel alias
    val ar = io.input.map((f: Axi4Lite) => f.ar)
    val r = io.input.map((f: Axi4Lite) => f.r)
    val aw = io.input.map((f: Axi4Lite) => f.aw)
    val w = io.input.map((f: Axi4Lite) => f.w)
    val b = io.input.map((f: Axi4Lite) => f.b)

    val arvalid = ar.map(_.valid).asBits
    val awvalid = aw.map(_.valid).asBits
    val wvalid  = w.map(_.valid).asBits
    val awwvalid = awvalid & wvalid // to synchronize AW and W channel

    // arbiter on each requester
    val readWriteArb = if (!parallelRW) new Area {
        val valid = arvalid | awwvalid // FXIME should be | but when changed to | it creates seg fault in verilator
        // at a given transaction, only one request and one response channel will fire
        val reqFire = io.output.ar.fire | io.output.aw.fire
        val respFire = io.output.r.fire | io.output.b.fire
        val pending = RegNextWhen(True, reqFire) clearWhen(respFire) init False
        val arbiter = RrArbiter(ar.length)
        arbiter.io.req <> valid
        arbiter.io.enable := reqFire
    } else null

    // arbiter on read channel
    val readArb = if (parallelRW) new Area {
        val pending = RegNextWhen(True, io.output.ar.fire) clearWhen(io.output.r.fire) init False
        val arbiter = RrArbiter(ar.length)
        arbiter.io.req <> arvalid
        arbiter.io.enable := io.output.ar.fire
    } else null

    // arbiter on write channel
    val writeArb = if (parallelRW) new Area {
        val pending = RegNextWhen(True, io.output.aw.fire) clearWhen(io.output.b.fire) init False
        val arbiter = RrArbiter(aw.length)
        arbiter.io.req <> awwvalid
        arbiter.io.enable := io.output.aw.fire
    } else null

    // ar channel
    val arGrant    = if (parallelRW) {readArb.arbiter.io.grant}      else {readWriteArb.arbiter.io.grant}
    val arGrantId  = if (parallelRW) {readArb.arbiter.io.grantId}    else {readWriteArb.arbiter.io.grantId}
    val arGranted  = if (parallelRW) {readArb.arbiter.io.prevGrant}  else {readWriteArb.arbiter.io.prevGrant}
    val arPending  = if (parallelRW) {readArb.pending}               else {readWriteArb.pending}
    // aw and w channel
    val awwGrant   = if (parallelRW) {writeArb.arbiter.io.grant}     else {readWriteArb.arbiter.io.grant}
    val awwGrantId = if (parallelRW) {writeArb.arbiter.io.grantId}   else {readWriteArb.arbiter.io.grantId}
    val awwGranted = if (parallelRW) {writeArb.arbiter.io.prevGrant} else {readWriteArb.arbiter.io.prevGrant}
    val awwPending = if (parallelRW) {writeArb.pending}              else {readWriteArb.pending}


    // AR channel
    ar.zipWithIndex.foreach(f => f._1.ready := io.output.ar.ready & arGrant(f._2) & ~arPending)
    io.output.ar.valid := (arvalid & arGrant.asBits).orR
    io.output.ar.payload := ar.map(_.payload).read(arGrantId)

    // R channel
    // Based on AXI spec, the read response will come at least one cycle after the request
    io.output.r.ready := (r.map(_.ready).asBits & arGranted.asBits).orR
    r.zipWithIndex.foreach(f => f._1.valid := io.output.r.valid & arGranted(f._2))
    r.foreach(_.payload := io.output.r.payload)

    // AW channel
    aw.zipWithIndex.foreach(f => f._1.ready := io.output.aw.ready & awwGrant(f._2) & ~awwPending)
    io.output.aw.valid := (awvalid & awwGrant.asBits).orR
    io.output.aw.payload := aw.map(_.payload).read(awwGrantId)

    // W channel
    w.zipWithIndex.foreach(f => f._1.ready := io.output.w.ready & awwGrant(f._2) & ~awwPending)
    io.output.w.valid := (wvalid & awwGrant.asBits).orR
    io.output.w.payload := w.map(_.payload).read(awwGrantId)

    // B channel
    io.output.b.ready := (b.map(_.ready).asBits & awwGranted.asBits).orR
    b.zipWithIndex.foreach(f => f._1.valid := io.output.b.valid & awwGranted(f._2))
    b.foreach(_.payload := io.output.b.payload)
}
