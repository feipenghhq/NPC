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
        val dbus = master(DbusBundle(config))
    }

    val iduData = io.iduData.payload
    val cpuCtrl = iduData.cpuCtrl

    io.iduData.ready := True

    // ----------------------------
    // ALU
    // ----------------------------
    val aluSrc1 = Mux(cpuCtrl.aluSelPc, iduData.pc.asBits, iduData.rs1Data)
    val aluSrc2 = Mux(cpuCtrl.selImm, cpuCtrl.immediate.asBits, iduData.rs2Data)

    val uAlu = ALU(config)
    uAlu.io.opcode <> cpuCtrl.opcode
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
    // Register Write Back
    // ----------------------------
    io.rdWrCtrl.payload.addr <> cpuCtrl.rdAddr
    io.rdWrCtrl.valid <> cpuCtrl.rdWrite
    io.rdWrCtrl.payload.data := Mux(cpuCtrl.memRead, uMeu.io.rdata, aluRes)
}


object EXUVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(EXU(config))
}