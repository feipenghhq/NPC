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

case class EXU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val iduData = slave Stream(IduBundle(config))
        val rdWrCtrl = master Flow(RdWrCtrl(config))
        val branchCtrl = master Flow(config.xlenUInt)
        val trapCtrl = master Flow(config.xlenUInt)
        val dbus = master(DbusBundle(config))
    }

    // path alias
    val iduData = io.iduData.payload
    val cpuCtrl = iduData.cpuCtrl
    val csrCtrl = iduData.csrCtrl
    val immediate = cpuCtrl.immediate.asBits

    // ----------------------------
    // Handshake
    // ----------------------------
    io.iduData.ready := True

    // ----------------------------
    // ALU
    // ----------------------------
    val aluSrc1 = Mux(cpuCtrl.aluSelPc, iduData.pc.asBits, iduData.rs1Data)
    val aluSrc2 = Mux(cpuCtrl.selImm,   immediate,         iduData.rs2Data)

    val uAlu = ALU(config)
    uAlu.io.opcode <> cpuCtrl.aluOpcode
    uAlu.io.src1 <> aluSrc1
    uAlu.io.src2 <> aluSrc2
    val aluRes = uAlu.io.result
    val aluAddRes = uAlu.io.addResult

    // ----------------------------
    // BEU
    // ----------------------------
    val uBeu = BEU(config)
    uBeu.io.branch <> cpuCtrl.branch
    uBeu.io.jump <> cpuCtrl.jump
    uBeu.io.opcode <> cpuCtrl.opcode
    uBeu.io.src1 <> iduData.rs1Data
    uBeu.io.src2 <> iduData.rs2Data
    uBeu.io.addr <> aluAddRes
    uBeu.io.branchCtrl <> io.branchCtrl

    // ----------------------------
    // MEU
    // ----------------------------
    val uMeu = MEU(config)
    io.dbus <> uMeu.io.dbus
    uMeu.io.memRead <> cpuCtrl.memRead
    uMeu.io.memWrite <> cpuCtrl.memWrite
    uMeu.io.opcode <> cpuCtrl.opcode
    uMeu.io.addr <> aluAddRes
    uMeu.io.wdata <> io.iduData.rs2Data

    // ----------------------------
    // CSR
    // ----------------------------
    val uCSR = CSR(config)
    uCSR.io.csrCtrl <> csrCtrl
    uCSR.io.csrWdata <> Mux(cpuCtrl.selImm, immediate, io.iduData.payload.rs1Data)

    // ----------------------------
    // TrapCtrl
    // ----------------------------
    val uTrapCtrl = TrapCtrl(config)
    uTrapCtrl.io.csrRdPort <> uCSR.io.csrRdPort
    uTrapCtrl.io.csrWrPort <> uCSR.io.csrWrPort
    uTrapCtrl.io.ecall <> cpuCtrl.ecall
    uTrapCtrl.io.mret <> cpuCtrl.mret
    uTrapCtrl.io.trapCtrl <> io.trapCtrl
    uTrapCtrl.io.trap <> uCSR.io.trap
    uTrapCtrl.io.pc <> io.iduData.pc

    // ----------------------------
    // Register Write Back
    // ----------------------------
    val pcPlus4 = iduData.pc + 4
    io.rdWrCtrl.payload.addr <> cpuCtrl.rdAddr
    io.rdWrCtrl.valid <> cpuCtrl.rdWrite
    io.rdWrCtrl.payload.data := Mux(csrCtrl.read,    uCSR.io.csrRdata,
                                Mux(cpuCtrl.memRead, uMeu.io.rdata,
                                Mux(cpuCtrl.jump,    pcPlus4.asBits,
                                                     aluRes.asBits)))
}


object EXUVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(EXU(config))
}