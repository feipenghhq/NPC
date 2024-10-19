/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/6/2024
 *
 * ------------------------------------------------------------------------------------------------
 * IFU: Instruction Fetch Unit
 * ------------------------------------------------------------------------------------------------
 * IFU contains the following logic:
 *  - Program Counter (PC)
 *  - Instruction memory read control
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import spinal.core.Verilator._
import config._
import _root_.bus.Axi4Lite._

case class IfuBundle(config: RiscCoreConfig) extends Bundle {
    val pc = config.xlenUInt
    val instruction = config.xlenBits
}

case class IFU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val ifuData = master Stream(IfuBundle(config))      // IFU data to next stage
        val branchCtrl = slave Flow(config.xlenUInt)        // branch control input
        val trapCtrl = slave Flow(config.xlenUInt)          // trap (exception/interrupt) control input
        val ibus = master(Axi4Lite(config.axi4LiteConfig))  // Instruction memory AXI bus
    }
    noIoPrefix()


    // -----------------------------
    // Program Counter (PC)
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

    // -------------------------------------
    // Instruction memory read control
    // -------------------------------------

    // An indicator to tell that we are out of reset and can start reading the instruction memory
    val start = RegNext(True) init False
    start.allowUnsetRegToAvoidLatch()

    // instruction read request
    val req = Reg(Bool) init True
    // Start to request for new instruction when the current instruction has been taken by the downstream logic
    when(io.ifuData.fire) {
        req := True
    // Clear the request when the instruction comes back
    }.elsewhen(io.ibus.r.fire) {
        req := False
    }

    // Read channel
    val ar = io.ibus.ar.payload
    ar.araddr  := pc
    ar.arid    := 0
    ar.arlen   := 0
    ar.arsize  := B"010" // always read 4 byte
    ar.arburst := 0
    io.ibus.arReq(start & req)
    io.ibus.r.ready := True // always able to take the read data
    // Write is not used for instruction memory
    io.ibus.aw <> io.ibus.aw.getZero
    io.ibus.w <> io.ibus.w.getZero
    io.ibus.b <> io.ibus.b.getZero

    // Need additional register to store the instruction comes back from instruction memory
    // when the downstream stage is not ready to take the new instruction.
    // This is needed because the memory does not preserve the output data.
    val instBuffer = RegNextWhen(io.ibus.r.payload.rdata(config.xlen-1 downto 0), io.ibus.r.fire)

    // The instruction is either from the instruction memory or the buffer.
    // When read data come back from memory, select it as the instruction.
    // In all the other cycles, select the instruction stored in the buffer.
    val instruction = config.xlenBits
    instruction.addAttribute(public)
    instruction := Mux(io.ibus.r.fire, io.ibus.r.payload.rdata(config.xlen-1 downto 0), instBuffer)

    // -----------------------------
    // IFU data logic
    // -----------------------------
    io.ifuData.pc := pc
    io.ifuData.instruction := instruction

    // -----------------------------
    // IFU data handshake
    // -----------------------------
    // set the IFU valid when we get the AXI read data, clear it when it is sent to the next stage
    val rdataReceived = RegNextWhen(True, io.ibus.r.fire)
    rdataReceived.clearWhen(io.ifuData.fire)
    io.ifuData.valid := io.ibus.r.fire | rdataReceived
}
