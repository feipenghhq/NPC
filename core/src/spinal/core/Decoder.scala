/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
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

/** Data path control signal. This goes to the next module/stage */
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
    val aluSelPc = Bool()
    val selImm = Bool()
    val opcode = Bits(5 bits)
    val rs1Addr = UInt(config.regidWidth bits)
    val rs2Addr = UInt(config.regidWidth bits)
    val immediate = config.xlenSInt
    // Optional based on ISA extension
    // RV32M
    val muldiv = config.hasRv32M generate Bool()
    // Zicsr
}

case class CsrCtrl(config: RiscCoreConfig) extends Bundle {
    val write =  Bool()
    val set = Bool()
    val clear = Bool()
    val read = Bool()
    val addr = UInt(12 bits)
}

case class Decoder(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val ifuData = slave Flow(IfuBundle(config))
        val cpuCtrl = master Flow(CpuCtrl(config))
        val csrCtrl = config.hasZicsr generate master Flow(CsrCtrl(config))
    }
    noIoPrefix()

    // alias some path
    val instruction = io.ifuData.payload.instruction
    val cpuCtrl = io.cpuCtrl.payload
    val csrCtrl = io.csrCtrl.payload

    // handshake signal
    io.cpuCtrl.valid := io.ifuData.valid

    // register address
    cpuCtrl.rdAddr  := instruction(11 downto 7).asUInt
    cpuCtrl.rs1Addr := instruction(19 downto 15).asUInt
    cpuCtrl.rs2Addr := instruction(24 downto 20).asUInt

    //-----------------------------------
    // Instruction decode
    //-----------------------------------

    val phase  = instruction(1 downto 0)
    val opcode = instruction(6 downto 2)
    val funct3 = instruction(14 downto 12)
    val funct7 = instruction(31 downto 25)

    val phase3 = phase === 3

    // Decode the instruction based on different opcode type
    val luiType = opcode === B"01101"
    val auipcType = opcode === B"00101"
    val jalType = opcode === B"11011"
    val jalrType = opcode === B"11001"
    val iType = opcode === B"00100"
    val rType = opcode === B"01100"
    val branchType = opcode === B"11000"
    val loadType = opcode === B"00000"
    val storeType = opcode === B"01000"
    val fenceType = opcode === B"00011"
    val systemType = opcode === B"11100"

    //-----------------------------------
    // generate control signal
    //-----------------------------------

    // Here we have some optimization, notice that for SLLI/SLLI/SLLI and R-type instruction,
    // funct7 is also used for instruction encoding but we didn't check it when generating the control
    // signal. This is a miss and may result into missing catching the illegal instruction exception

    cpuCtrl.aluSelPc := jalType | auipcType | branchType
    cpuCtrl.selImm := jalType | jalrType | branchType | luiType | auipcType | iType | loadType | storeType

    cpuCtrl.rdWrite := luiType | auipcType | jalType | jalrType | iType | rType | loadType
    cpuCtrl.branch := branchType
    cpuCtrl.jump := jalType | jalrType
    cpuCtrl.memRead := loadType
    cpuCtrl.memWrite := storeType
    cpuCtrl.ebreak := systemType & (instruction(31 downto 7) === B"25'x2000")
    cpuCtrl.ecall := systemType & (instruction(31 downto 7) === B"25'x0" )

    // opcode(2 downto 0): same encoding as funct3 or add for some instruction
    // opcode(3): distinguish add/sub, srl(i)/sra(i) same as instruction[30]
    // opcode(4): lui instruction for ALU
    val opcodeAdd = branchType | luiType | auipcType | jalType | jalrType | loadType | storeType
    cpuCtrl.opcode(2 downto 0) := Mux(opcodeAdd, B"000", funct3)
    cpuCtrl.opcode(3) := Mux(opcodeAdd, False, instruction(30))
    cpuCtrl.opcode(4) := luiType

    //-----------------------------------
    // generate control signal for ISA extension
    //-----------------------------------

    // Rv32M
    if (config.hasRv32M) {
        cpuCtrl.muldiv := rType & (funct7 === 1)
    }

    // Zicsr
    if (config.hasZicsr) {
        // csrrw/csrrwi should not read CSR if rd = x0
        // csrrs(i)/csrrc(i) should not write CSR if rs1 = x0 (uimm = 0)
        val csrRead = cpuCtrl.rdAddr =/= 0
        val csrWrite = cpuCtrl.rs1Addr =/= 0

        val csrrw = systemType & (funct3(2 downto 0) === 1)
        val csrrs = systemType & (funct3(2 downto 0) === 2)
        val csrrc = systemType & (funct3(2 downto 0) === 3)

        csrCtrl.write := csrrw
        csrCtrl.set := csrrs & csrWrite
        csrCtrl.clear := csrrc & csrWrite
        csrCtrl.read := csrrw & csrRead | csrrs | csrrc
        csrCtrl.addr := instruction(31 downto 20).asUInt
    }

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

    val cTypeImm = if (config.hasZicsr) systemType & funct3(2) else False
    val cTypeImmVal = if (config.hasZicsr) (cpuCtrl.rs1Addr).resize(config.xlen).asSInt else S(0, config.xlen bits)

    val immSel   = Vec(iTypeImm,    uTypeImm,    jTypeImm,    sTypeImm,    bTypeImm,    cTypeImm)
    val immValue = Vec(iTypeImmVal, uTypeImmVal, jTypeImmVal, sTypeImmVal, bTypeImmVal, cTypeImmVal)

    cpuCtrl.immediate := OHMux(immSel, immValue)
}

object Decoder {
    def apply(config: RiscCoreConfig, ifuData: Stream[IfuBundle], cpuCtrl: CpuCtrl): Decoder = {
        val decoder = Decoder(config)
        decoder.io.ifuData.valid := ifuData.valid
        decoder.io.ifuData.payload <> ifuData.payload
        decoder.io.cpuCtrl.payload <> cpuCtrl
        decoder
    }
}

object DecoderVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(Decoder(config))
}
