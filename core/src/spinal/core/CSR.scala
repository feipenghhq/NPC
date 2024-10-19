/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/13/2024
 *
 * ------------------------------------------------------------------------------------------------
 * CSR: Control and Status Register
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._
import scala.collection.mutable.ArrayBuffer

case class CsrWrPort(config: RiscCoreConfig) extends Bundle {
    val mepc = in port config.xlenBits
    val mcause = in port config.xlenBits
}

case class CsrRdPort(config: RiscCoreConfig) extends Bundle {
    val mtvec = out port config.xlenBits
    val mepc = out port config.xlenBits
}

case class CSR(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val csrCtrl = in port CsrCtrl(config)
        val csrWdata = in port config.xlenBits
        val csrRdata = out port config.xlenBits
        val trap = in port Bool()
        val csrRdPort = CsrRdPort(config)
        val csrWrPort = CsrWrPort(config)
    }
    noIoPrefix()

    // signal alias
    val csrCtrl = io.csrCtrl
    val trap = io.trap
    val rdPort = io.csrRdPort
    val wrPort = io.csrWrPort
    val xlen = config.xlen

    // create the final CSR write data based on different access type
    val csrwrite = csrCtrl.write | csrCtrl.set | csrCtrl.clear
    // For csrrw(i), csrWdata is directly written in to csr
    val writeData = io.csrWdata
    // For csrrs(i), csrWdata is used as a bit mask that specifies bit position to be set in csr.
    val setData = io.csrWdata | io.csrRdata
    // For csrrc(i), csrWdata is used as a bit mask that specifies bit positions to be cleared in csr.
    val clearData = ~io.csrWdata & io.csrRdata
    // Final write data
    val wdata = MuxOH(Vec(csrCtrl.write, csrCtrl.set, csrCtrl.clear),
                      Vec(writeData,     setData,     clearData))

    // csr read MUX
    val readSel = ArrayBuffer[Bool]()
    val readData = ArrayBuffer[Bits]()

    /** A generic CSR class
     *  It hold the information such as name, address of the csr
     *  It also field
     *
     *  @param csrName csr name
     *  @param addr    csr address
     */
    case class CsrReg(csrName: String, addr: Int) {
        val reg = B(0, xlen bits).allowOverride // This is not an actual register,
                                                // it's only wires that groups all the field.
        val hit = csrCtrl.addr === addr
        val read = hit & csrCtrl.read
        val write = hit & csrwrite

        reg.setName(csrName)
        hit.setName(csrName + "Hit")
        read.setName(csrName + "Read")
        write.setName(csrName + "Write")

        // add the csr to read MUX logic
        readSel.append(read)
        readData.append(reg)

        /**
          * Function to add a field to the register
          * It defines an actual hardware register for the field and perform read/write logic to that field
          *
          * @param fieldName Name of the register field. Example: mstatus
          * @param range     Range of the field in the register. Example: (31 downto 0)
          * @param rst       Reset value for the field
          * @param cpuWr     1: cpu write this field through csr logic. 0: cpu don't write to this field
          * @param rdPort    hardware signal that read this field (usually for config register). null means not needed
          * @param wr        hardware signal write enable (usually for status register). null means not needed
          * @param wrPort    hardware signal that write to this field (usually for status register). null means not needed
          */
        def addField(fieldName: String, range: Range, rst: Int = 0, cpuWr: Boolean = true,
                     rdPort: Bits = null, wr: Bool = null, wrPort: Bits = null): Unit = {
            val field = Reg(Bits(range.length bits)).init(rst).allowUnsetRegToAvoidLatch
            field.setName(csrName + fieldName.capitalize)
            reg(range) := field
            // write logic
            val wb = WhenBuilder()
            if (cpuWr) wb.when(write) {field := wdata(range)}
            if (wr != null) wb.elsewhen(wr) {field := wrPort}
            // read logic
            if (rdPort != null) {rdPort := field}
        }

    }

    // ---------------------------------------------------
    // Add csr register
    // ---------------------------------------------------
    val mstatus = CsrReg("mstatus", 0x300)
    mstatus.addField("mstatus", (xlen-1 downto 0), 0x1800)

    val mtvec = CsrReg("mtvec", 0x305)
    mtvec.addField("base", (xlen-1 downto 2), 0, rdPort=rdPort.mtvec(xlen-1 downto 2))
    mtvec.addField("mode", (1 downto 0),      0, rdPort=rdPort.mtvec(1 downto 0))

    val mepc = CsrReg("mepc", 0x341)
    mepc.addField("mepc", (xlen-1 downto 0), 0, wr=trap, wrPort=wrPort.mepc, rdPort=rdPort.mepc)

    val mcause = CsrReg("mcause", 0x342)
    mcause.addField("interrupt",     (xlen-1 downto xlen-1), 0, wr=trap, wrPort=wrPort.mcause(xlen-1).asBits)
    mcause.addField("exceptionCode", (xlen-2 downto 0),      0, wr=trap, wrPort=wrPort.mcause(xlen-2 downto 0))
    // ---------------------------------------------------

    // read data mux
    io.csrRdata := OHMux(readSel, readData)
}
