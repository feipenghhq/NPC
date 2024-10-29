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
 * Currently the IFU support only Multiple Cycle CPU
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
    // Instruction memory read control (AXI bus)
    // -------------------------------------

    // An indicator to tell that we are out of reset and can start sending the first request
    val start = Reg(Bool) init False
    start := True

    val arvalid = Reg(Bool) init False
    // assert arvalid when
    // 1. cpu is out of rest
    // 2. the downstream logic has consumed the instruction
    when(start.rise() || io.ifuData.fire) {
        arvalid := True
    }
    // lower the arvalid when the AR channel handshake complete
    .elsewhen(io.ibus.ar.fire) {
        arvalid := False
    }

    io.ibus.ar.valid := arvalid
    io.ibus.ar.payload.araddr := pc

    // IFU is always able to take the read response data
    // This is guaranteed because we only send one request till the returned instruction has been consumed
    if (config.ifuRreadyDelay == 0) {
        io.ibus.r.ready := True
    }
    // Add delay to test back-pressure on axi bus
    else {
        val rready = Timeout(config.ifuRreadyDelay)
        when(io.ibus.ar.fire) {rready.clear()}
        io.ibus.r.ready := rready
    }

    // Write is not used for instruction memory
    io.ibus.aw <> io.ibus.aw.getZero
    io.ibus.w <> io.ibus.w.getZero
    io.ibus.b <> io.ibus.b.getZero

    // Need additional buffer to store the instruction coming back from memory to handle the stall cases
    // When the downstream logic is not able to consume instruction, store it in the buffer.
    // When it is ready, then forward the instruction from the buffer.
    val instBuffer = RegNextWhen(io.ibus.r.payload.rdata(config.xlen-1 downto 0), io.ibus.r.fire)
    val instBufferValid = RegNextWhen(True, io.ibus.r.fire & ~io.ifuData.fire).clearWhen(io.ifuData.fire)
    val instruction = config.xlenBits
    instruction := Mux(io.ibus.r.fire, io.ibus.r.payload.rdata(config.xlen-1 downto 0), instBuffer)
    instruction.addAttribute(public)

    // -----------------------------
    // IFU data logic
    // -----------------------------
    io.ifuData.pc := pc
    io.ifuData.instruction := instruction

    // -----------------------------
    // IFU data handshake
    // -----------------------------
    io.ifuData.valid := io.ibus.r.fire | instBufferValid
}
