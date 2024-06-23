/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/10/2024
 *
 * ------------------------------------------------------------------------------------------------
 * EXU: Execution Unit
 * ------------------------------------------------------------------------------------------------
 */
package core

import spinal.core._
import spinal.lib._
import config._
import _root_.bus.Axi4Lite._
import spinal.core.Verilator._

case class EXU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val iduData = slave Stream(IduBundle(config))
        val rdWrCtrl = master Flow(RdWrCtrl(config))
        val branchCtrl = master Flow(config.xlenUInt)
        val trapCtrl = master Flow(config.xlenUInt)
        val dbus = master(Axi4Lite(config.axi4LiteConfig))
    }

    // ----------------------------
    // path alias
    // ----------------------------
    val iduData = io.iduData.payload
    val cpuCtrl = iduData.cpuCtrl
    val csrCtrl = iduData.csrCtrl
    val immediate = cpuCtrl.immediate.asBits

    // ----------------------------
    // Instantiate all modules
    // ----------------------------
    val uAlu = ALU(config)
    val uMulDiv = MulDiv(config)
    val uBeu = BEU(config)
    val uLsu = LSU(config)
    val uCSR = CSR(config)
    val uTrapCtrl = TrapCtrl(config)

    // ----------------------------
    // Connection and glue logic
    // ----------------------------

    // ALU
    val aluSrc1 = Mux(cpuCtrl.aluSelPc, iduData.pc.asBits, iduData.rs1Data)
    val aluSrc2 = Mux(cpuCtrl.selImm,   immediate,         iduData.rs2Data)

    uAlu.io.opcode <> cpuCtrl.aluOpcode
    uAlu.io.src1 <> aluSrc1
    uAlu.io.src2 <> aluSrc2
    val aluRes = uAlu.io.result
    val aluAddRes = uAlu.io.addResult

    // MulDiv
    uMulDiv.io.opcode <> cpuCtrl.opcode
    uMulDiv.io.src1 <> aluSrc1
    uMulDiv.io.src2 <> aluSrc2

    // BEU
    uBeu.io.branch <> cpuCtrl.branch
    uBeu.io.jump <> cpuCtrl.jump
    uBeu.io.opcode <> cpuCtrl.opcode
    uBeu.io.src1 <> iduData.rs1Data
    uBeu.io.src2 <> iduData.rs2Data
    uBeu.io.addr <> aluAddRes
    uBeu.io.branchCtrl <> io.branchCtrl

    // MEU
    io.dbus <> uLsu.io.dbus
    uLsu.io.memRead <> cpuCtrl.memRead
    uLsu.io.memWrite <> cpuCtrl.memWrite
    uLsu.io.opcode <> cpuCtrl.opcode
    uLsu.io.addr <> aluAddRes
    uLsu.io.wdata <> io.iduData.rs2Data

    // CSR
    uCSR.io.csrCtrl <> csrCtrl
    uCSR.io.csrWdata <> Mux(cpuCtrl.selImm, immediate, io.iduData.payload.rs1Data)

    // TrapCtrl
    uTrapCtrl.io.csrRdPort <> uCSR.io.csrRdPort
    uTrapCtrl.io.csrWrPort <> uCSR.io.csrWrPort
    uTrapCtrl.io.ecall <> cpuCtrl.ecall
    uTrapCtrl.io.mret <> cpuCtrl.mret
    uTrapCtrl.io.trapCtrl <> io.trapCtrl
    uTrapCtrl.io.trap <> uCSR.io.trap
    uTrapCtrl.io.pc <> io.iduData.pc

    // Register Write Back
    val pcPlus4 = iduData.pc + 4
    io.rdWrCtrl.payload.addr <> cpuCtrl.rdAddr
    io.rdWrCtrl.valid := cpuCtrl.rdWrite & io.iduData.fire
    io.rdWrCtrl.payload.data := Mux(csrCtrl.read,    uCSR.io.csrRdata,
                                Mux(cpuCtrl.memRead, uLsu.io.rdata,
                                Mux(cpuCtrl.jump,    pcPlus4.asBits,
                                Mux(cpuCtrl.muldiv,  uMulDiv.io.result.asBits,
                                                     aluRes.asBits))))

    // ----------------------------
    // Handshake
    // ----------------------------
    val stall = cpuCtrl.memRead & ~uLsu.io.rvalid
    io.iduData.ready := ~stall

    // for simulation
    val done = io.iduData.fire
    done.addAttribute(public)
}


object EXUVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(EXU(config))
}