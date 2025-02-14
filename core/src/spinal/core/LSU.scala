/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
 * Author: Heqing Huang
 * Date Created: 6/11/2024
 *
 * ------------------------------------------------------------------------------------------------
 * LSU: Load Store Unit
 * Currently the LSU support only Multiple Cycle CPU
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._
import config._
import _root_.bus.Axi4Lite.Axi4Lite

case class LSU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val dbus     = master(Axi4Lite(config.axi4LiteConfig))
        val memRead  = in port Bool()
        val memWrite = in port Bool()
        val opcode   = in port Bits(3 bits)
        val addr     = in port config.xlenUInt
        val wdata    = in port config.xlenBits
        val rdata    = out port config.xlenBits
        val wready   = out port Bool()
        val rvalid   = out port Bool()
    }
    noIoPrefix()

    val numByte = config.xlen / 8
    val numHalf = config.xlen / 16
    val numWord = config.xlen / 32

    val selAddr = io.addr(1 downto 0)

    // ---------------------------------------
    // LSU Main State Machine
    // ---------------------------------------
    val lsuCtrl = new StateMachine {
        setEncoding(binaryOneHot)
        val IDLE: State = makeInstantEntry()
        val RD_REQ, WR_REQ, DATA, RESP: State = new State

        // logic to make sure both AW and W channel completes handshake
        val aw_cmp = Reg(Bool) init False
        val w_cmp = Reg(Bool) init False
        val wr_cmp = io.dbus.aw.ready & io.dbus.w.ready | // both the AW and W channels are ready to receive request
                     io.dbus.aw.ready & w_cmp |           // AW channel is ready and W channels has completed the handshake
                     io.dbus.w.ready  & aw_cmp            // W channel is ready and AW channel has completed the handshake
        when(io.dbus.b.fire) { aw_cmp := False }.elsewhen( io.dbus.aw.fire ) { aw_cmp := True }
        when(io.dbus.b.fire) { w_cmp := False  }.elsewhen( io.dbus.w.fire  ) { w_cmp := True  }

        IDLE.whenIsActive {
            when (io.memRead) {
                goto(RD_REQ)
            }
            when (io.memWrite) {
                goto(WR_REQ)
            }
        }

        RD_REQ.whenIsActive {
            when (io.dbus.ar.ready) {
                goto(DATA)
            }
        }

        WR_REQ.whenIsActive {
            when (wr_cmp) {
                goto(RESP)
            }
        }

        DATA.whenIsActive {
            when (io.dbus.r.fire) {
                goto(IDLE)
            }

        }

        RESP.whenIsActive {
            when (io.dbus.b.fire) {
                goto(IDLE)
            }

        }

    }


    // ---------------------------------------
    // Write Logic
    // ---------------------------------------

    // aw channel
    io.dbus.aw.valid := lsuCtrl.isActive(lsuCtrl.WR_REQ) & ~lsuCtrl.aw_cmp
    io.dbus.aw.payload.awaddr := io.addr

    // w channel
    io.dbus.w.valid := lsuCtrl.isActive(lsuCtrl.WR_REQ) & ~lsuCtrl.w_cmp

    // 1. wstrb can be obtains by using the byte offset (selAddr) to shift the mask to the correct position
    // 2. data is duplicated and placed in all the chunks
    val w = io.dbus.w.payload
    switch(io.opcode(1 downto 0)) {
        is(0) { // SB
            w.wstrb := B(1, w.wstrb.getWidth bits) |<< selAddr
            w.wdata := io.wdata(7 downto 0) #* numByte
        }
        is(1) { // SH
            w.wstrb := B(3, w.wstrb.getWidth bits) |<< selAddr
            w.wdata := io.wdata(15 downto 0) #* numHalf
        }
        default { // SW
            w.wstrb := B(15, w.wstrb.getWidth bits) |<< selAddr
            w.wdata := io.wdata #* numWord
        }
    }

    // b channel
    // we can move forward when the write response comes back
    // and for now, we just ignore the write response result and treat write as always successful
    io.wready := io.dbus.b.fire

    // IFU is always capable of consuming the write response
    io.dbus.b.ready := True

    // ---------------------------------------
    // Read logic
    // ---------------------------------------

    // ar channel
    io.dbus.ar.valid := lsuCtrl.isActive(lsuCtrl.RD_REQ) // Assert the valid signal in RD_REG state
    io.dbus.ar.payload.araddr := io.addr

    // r channel
    // IFU is always capable of consuming the write response
    io.dbus.r.ready := True

    // select the correct data portion based on the address
    val byteData = io.dbus.r.payload.rdata.subdivideIn(8 bits).read(selAddr(selAddr.getWidth-1 downto 0))
    val halfData = io.dbus.r.payload.rdata.subdivideIn(16 bits).read(selAddr(selAddr.getWidth-1 downto 1))
    val wordData = io.dbus.r.payload.rdata
    switch(io.opcode(2 downto 0)) {
        is(0) { io.rdata := byteData.asSInt.resize(config.xlen bits).asBits } // LB
        is(1) { io.rdata := halfData.asSInt.resize(config.xlen bits).asBits } // LH
        is(4) { io.rdata := byteData.resized }                                // LBU
        is(5) { io.rdata := halfData.resized }                                // LHU
        default { io.rdata := wordData.resized }                              // LW
    }
    io.rvalid := io.dbus.r.fire
}
