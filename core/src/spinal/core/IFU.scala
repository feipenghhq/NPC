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
import spinal.lib.fsm._
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

    val ifuCtrl = new StateMachine {
        setEncoding(binaryOneHot)
        val IDLE: State = makeInstantEntry()
        val REQ, DATA, STALL: State = new State

        val instruction = config.xlenBits
        val instBuffer = Reg(config.xlenBits)
        instruction := io.ibus.r.payload.rdata

        IDLE.whenIsActive {
            goto(REQ)
        }

        REQ.whenIsActive {
            // When AR channel handshake complete goto DATA state to wait for read data
            when(io.ibus.ar.ready) {
                goto(DATA)
            }
        }

        DATA.whenIsActive {
            // when data is returned and ifu can move forward, goto REQ state again
            when(io.ibus.r.valid && io.ifuData.ready) {
                goto(REQ)
            }
            // when data is returned but ifu is stalled, goto STALL state
            // meanwhile, store the return data (instruction) to instruction buffer
            .elsewhen(io.ibus.r.valid && !io.ifuData.ready) {
                instBuffer := io.ibus.r.payload.rdata
                goto(STALL)
            }
        }

        STALL.whenIsActive {
            // instruction is selected from instruction buffer
            instruction := instBuffer
            // ifu is ready to go, go to REQ state again
            when(io.ifuData.ready) {
                goto(REQ)
            }
        }
    }

    io.ibus.ar.valid := ifuCtrl.isActive(ifuCtrl.REQ) // assert arvalid at REQ state
    io.ibus.ar.payload.araddr := pc
    io.ibus.r.ready := True // always ready to receive data

    // Write is not used for instruction memory
    io.ibus.aw <> io.ibus.aw.getZero
    io.ibus.w <> io.ibus.w.getZero
    io.ibus.b <> io.ibus.b.getZero

    // -----------------------------
    // IFU data logic
    // -----------------------------
    io.ifuData.pc := pc
    io.ifuData.instruction := ifuCtrl.instruction

    // -----------------------------
    // IFU data handshake
    // -----------------------------
    io.ifuData.valid := io.ibus.r.fire | ifuCtrl.isActive(ifuCtrl.STALL)

}
