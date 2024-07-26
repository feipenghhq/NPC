/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/6/2024
 *
 * ------------------------------------------------------------------------------------------------
 * IFU: Instruction Fetch Unit
 * ------------------------------------------------------------------------------------------------
 * IFU contains the PC and the logic to access the instruction memory
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._
import spinal.core.Verilator._
import _root_.bus.Axi4Lite._

case class IfuBundle(config: RiscCoreConfig) extends Bundle {
    val pc = config.xlenUInt
    val instruction = config.xlenBits
}

case class IFU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val ifuData = master Stream(IfuBundle(config))
        val branchCtrl = slave Flow(config.xlenUInt)
        val trapCtrl = slave Flow(config.xlenUInt)
        val ibus = master(Axi4Lite(config.axi4LiteConfig))
    }
    noIoPrefix()

    val start = RegNext(True) init False
    start.allowUnsetRegToAvoidLatch()

    // -----------------------------
    // PC logic
    // -----------------------------
    val nextPC = config.xlenUInt
    nextPC.addAttribute(public)

    val pc = RegNextWhen(nextPC, io.ifuData.fire) init (config.pcRstVector)
    pc.addAttribute(public)

    when(io.trapCtrl.valid) {
        nextPC := io.trapCtrl.payload
    } elsewhen(io.branchCtrl.valid) {
        nextPC := io.branchCtrl.payload
    } otherwise {
        nextPC := pc + 4
    }

    // -----------------------------
    // Instruction logic
    // -----------------------------
    // Need additional register to store the instruction when the downstream stage
    // is not ready to take the new instruction when it comes back because the memory
    // does not preserve the output data.
    val instBuffer = RegNextWhen(io.ibus.r.payload.rdata(config.xlen-1 downto 0), io.ibus.r.fire)
    val instruction = config.xlenBits
    instruction.addAttribute(public)
    instruction := Mux(io.ibus.r.fire, io.ibus.r.payload.rdata(config.xlen-1 downto 0), instBuffer)

    // -----------------------------
    // IFU data logic
    // -----------------------------
    io.ifuData.pc := pc
    io.ifuData.instruction := instruction

    // -----------------------------
    // IBUS logic
    // -----------------------------
    val arvalid = RegNextWhen(True, io.ifuData.fire) init True
    val arvalidClear = io.ibus.r.fire & ~io.ifuData.fire
    arvalid.clearWhen(arvalidClear)


    val ar = io.ibus.ar.payload
    ar.araddr  := pc
    ar.arid    := 0
    ar.arlen   := 0
    ar.arsize  := B"010" // 4 byte
    ar.arburst := 0
    io.ibus.arReq(start & arvalid)
    io.ibus.r.ready := True
    io.ibus.aw <> io.ibus.aw.getZero
    io.ibus.w <> io.ibus.w.getZero
    io.ibus.b <> io.ibus.b.getZero

    // -----------------------------
    // handshake
    // -----------------------------

    // set the IFU valid when we get the AXI read data, clear it when it is sent to the next stage
    val rdataReceived = RegNextWhen(True, io.ibus.r.fire)
    rdataReceived.clearWhen(io.ifuData.fire)

    io.ifuData.valid := io.ibus.r.fire | rdataReceived
}
