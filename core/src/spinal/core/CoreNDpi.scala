/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/15/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Black box for CoreNDPI
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._

case class CoreNDpi (config: RiscCoreConfig) extends BlackBox {

  val generic = new Generic {
    val XLEN = config.xlen
  }

  val io = new Bundle {
    val clk = in port Bool()
    val rst_b = in port Bool()
    val ebreak = in port Bool()
    val ecall = in port Bool()
    val pc = in port config.xlenUInt
  }

  noIoPrefix()
  mapClockDomain(clock = io.clk, reset = io.rst_b, resetActiveLevel = LOW)
}

