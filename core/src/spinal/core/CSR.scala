/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
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
        val csrCtrl = slave Flow(CsrCtrl(config))
        val csrWdata = in port config.xlenBits
        val csrRdata = out port config.xlenBits
        val trap = in port Bool()
        val csrRdPort = CsrRdPort(config)
        val csrWrPort = CsrWrPort(config)
    }
    noIoPrefix()

    // signal alias
    val csrCtrl = io.csrCtrl.payload
    val trap = io.trap
    val rdPort = io.csrRdPort
    val wrPort = io.csrWrPort
    val xlen = config.xlen

    // create the final CSR write data based on different access type
    val csrwrite = csrCtrl.write | csrCtrl.set | csrCtrl.clear
    val writeData = io.csrWdata
    val setData = io.csrWdata | io.csrRdata
    val clearData = ~io.csrWdata & io.csrRdata
    val wdata = MuxOH(Vec(csrCtrl.write, csrCtrl.set, csrCtrl.clear),
                      Vec(writeData,     setData,     clearData))

    // CSR read data mux placeholder
    val readSel = ArrayBuffer[Bool]()
    val readData = ArrayBuffer[Bits]()

    // class to hold information for CSR and its field
    case class CsrReg(csrName: String, addr: Int) {
        //val reg = config.xlenBits
        val reg = B(0, xlen bits).allowOverride
        val hit = csrCtrl.addr === addr
        val read = hit & csrCtrl.read
        val write = hit & csrwrite

        reg.setName(csrName)
        hit.setName(csrName + "Hit")
        read.setName(csrName + "Read")
        write.setName(csrName + "Write")

        readSel.append(read)
        readData.append(reg)

        def addField(fieldName: String, range: Range, rst: Int = 0, cpuWr: Boolean = true,
                     rdPort: Bits = null, wr: Bool = null, wrPort: Bits = null): Unit = {
            val field = Reg(Bits(range.length bits)).init(rst).allowUnsetRegToAvoidLatch
            field.setName(csrName + fieldName.capitalize)
            reg(range) := field
            // write logic
            val wb = WhenBuilder()
            if (cpuWr) wb.when(write) {field := wdata(range)}
            if (wr != null) wb.when(wr) {field := wrPort}
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

object CSRVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(CSR(config)).printPruned()
}

