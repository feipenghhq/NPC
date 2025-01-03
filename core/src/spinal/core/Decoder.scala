/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/9/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Decoder: Instruction Decode Unit
 * ------------------------------------------------------------------------------------------------
 * Decode the Instruction into different cpu control signal
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._

/**
 * Data path control signal. This goes to the next module/stage
 */
case class CpuCtrl(config: RiscCoreConfig) extends Bundle {
    // RV32I
    val rdWrite = Bool()
    val rdAddr = UInt(config.regidWidth bits)
    val branch = Bool()
    val jump = Bool()
    val memRead = Bool()
    val memWrite = Bool()
    val ebreak = Bool()
    val ecall = Bool()
    val mret  = Bool()
    val aluSelPc = Bool()
    val selImm = Bool()
    val aluOpcode = Bits(5 bits)
    val opcode = Bits(3 bits)
    val rs1Addr = UInt(config.regidWidth bits)
    val rs2Addr = UInt(config.regidWidth bits)
    val immediate = config.xlenSInt
    val muldiv = Bool()
}

/**
  * CSR related control signal
  */
case class CsrCtrl(config: RiscCoreConfig) extends Bundle {
    val write =  Bool()
    val set = Bool()
    val clear = Bool()
    val read = Bool()
    val addr = UInt(12 bits)
}

case class Decoder(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val ifuData = in  port IfuBundle(config)
        val cpuCtrl = out port CpuCtrl(config)
        val csrCtrl = out port CsrCtrl(config)
    }
    noIoPrefix()

    // alias some path
    val instruction = io.ifuData.instruction
    val cpuCtrl = io.cpuCtrl
    val csrCtrl = io.csrCtrl

    //-----------------------------------
    // Instruction decode
    //-----------------------------------

    // Extract instruction field
    cpuCtrl.rdAddr  := instruction(11 downto 7).asUInt
    cpuCtrl.rs1Addr := instruction(19 downto 15).asUInt
    cpuCtrl.rs2Addr := instruction(24 downto 20).asUInt

    val phase  = instruction(1 downto 0)
    val opcode = instruction(6 downto 2)
    val funct3 = instruction(14 downto 12)
    val funct7 = instruction(31 downto 25)

    // Decode the instruction based on different opcode type
    val phase3 = phase === 3

    val luiType    = phase3 & opcode === B"01101"
    val auipcType  = phase3 & opcode === B"00101"
    val jalType    = phase3 & opcode === B"11011"
    val jalrType   = phase3 & opcode === B"11001"
    val iType      = phase3 & opcode === B"00100"
    val rType      = phase3 & opcode === B"01100"
    val branchType = phase3 & opcode === B"11000"
    val loadType   = phase3 & opcode === B"00000"
    val storeType  = phase3 & opcode === B"01000"
    val fenceType  = phase3 & opcode === B"00011"
    val systemType = phase3 & opcode === B"11100"

    //-----------------------------------------------------
    // generate control signal for basic RV32I ISA
    //-----------------------------------------------------

    // WARNING: here we have some optimization. Notice that for slli/slli/slli and r-type instruction,
    // funct7 is also used for instruction encoding but we didn't check it when generating the control
    // signal. This is a miss and may result into failure to catch the illegal instruction exception

    cpuCtrl.aluSelPc := jalType | auipcType | branchType
    cpuCtrl.selImm   := jalType | jalrType  | branchType | luiType  | auipcType | iType | loadType | storeType
    cpuCtrl.rdWrite  := luiType | auipcType | jalType    | jalrType | iType     | rType | loadType | csrCtrl.read
    cpuCtrl.branch   := branchType
    cpuCtrl.jump     := jalType | jalrType
    cpuCtrl.memRead  := loadType
    cpuCtrl.memWrite := storeType
    cpuCtrl.ebreak   := systemType & (instruction(31 downto 7) === B"25'x2000")
    cpuCtrl.ecall    := systemType & (instruction(31 downto 7) === B"25'x0" )
    cpuCtrl.mret     := systemType & (instruction(31 downto 7) === B"25'x604000")

    // ALU opcode encoding:
    // aluOpcode[2:0]: Same encoding as funct3 field because it encodes most of the logic/arithmetic operation.
    //                 Or 3'b000 (Add) for some instructions that use the add function in ALU.
    // aluOpcode[3]:   Used to distinguish between add/sub, srl(i)/sra(i). This info is encoded in instruction[30].
    // aluOpcode[4]:   Denote lui instruction for ALU
    val opcodeAdd = branchType | luiType | auipcType | jalType | jalrType | loadType | storeType
    cpuCtrl.aluOpcode(2 downto 0) := Mux(opcodeAdd, B"000", funct3)
    cpuCtrl.aluOpcode(3) := instruction(30) & (
                            ((iType | rType) & (funct3 === B"101")) | // srl(i)/sra(i)
                             (rType          & (funct3 === B"000")))  // add/sub
    cpuCtrl.aluOpcode(4) := luiType

    // Opcode for branch/store/load/muldiv:
    // Again, the RISC-V spec use funct3 to encode these operation so just use funct3 as well
    cpuCtrl.opcode := funct3

    //-----------------------------------------------------
    // generate control signal for optional ISA
    //-----------------------------------------------------

    // -- RV32M --
    cpuCtrl.muldiv := rType & (funct7 === 1)

    // -- Zicsr --

    // csrrw/csrrwi should not read CSR if rd = x0
    // csrrs(i)/csrrc(i) should not write CSR if rs1 = x0 (uimm = 0)
    val csrRead = cpuCtrl.rdAddr =/= 0
    val csrWrite = cpuCtrl.rs1Addr =/= 0

    val csrrw = systemType & (funct3 === 1)
    val csrrs = systemType & (funct3 === 2)
    val csrrc = systemType & (funct3 === 3)

    csrCtrl.write := csrrw
    csrCtrl.set   := csrrs & csrWrite
    csrCtrl.clear := csrrc & csrWrite
    csrCtrl.read  := csrrw & csrRead | csrrs | csrrc
    csrCtrl.addr  := instruction(31 downto 20).asUInt

    //-----------------------------------
    // generate Immediate
    //-----------------------------------

    val iTypeImm = iType
    val iTypeImmVal = (instruction(31) ## instruction(31 downto 20)).asSInt.resize(config.xlen)

    val uTypeImm = luiType | auipcType
    val uTypeImmVal = (instruction(31 downto 12) ## U((11 downto 0) -> false)).asSInt.resize(config.xlen)

    val jTypeImm = jalType
    val jTypeImmVal = (instruction(31) ## instruction(19 downto 12) ## instruction(20) ## instruction(30 downto 21) ## False ).asSInt.resize(config.xlen)

    val sTypeImm = storeType
    val sTypeImmVal = (instruction(31) ## instruction(31 downto 25) ## instruction(11 downto 7)).asSInt.resize(config.xlen)

    val bTypeImm = branchType
    val bTypeImmVal = (instruction(31) ## instruction(7) ## instruction(30 downto 25) ## instruction(11 downto 8) ## False).asSInt.resize(config.xlen)

    val cTypeImm = systemType & funct3(2)
    val cTypeImmVal = (cpuCtrl.rs1Addr).resize(config.xlen).asSInt

    val immSel   = Vec(iTypeImm,    uTypeImm,    jTypeImm,    sTypeImm,    bTypeImm,    cTypeImm)
    val immValue = Vec(iTypeImmVal, uTypeImmVal, jTypeImmVal, sTypeImmVal, bTypeImmVal, cTypeImmVal)

    cpuCtrl.immediate := OHMux(immSel, immValue)
}
