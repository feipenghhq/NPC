/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/22/2024
 *
 * ------------------------------------------------------------------------------------------------
 * Black box for SramDpi
 * ------------------------------------------------------------------------------------------------
 */

package misc

import spinal.core._
import spinal.lib._
import config._

case class SramDpi (config: RiscCoreConfig) extends BlackBox {

  val generic = new Generic {
    val XLEN = config.xlen
  }

  val io = new Bundle {
    val clk = in port Bool()
    val rst_b = in port Bool()
    val ifetch = in port Bool()
    val pc = in port config.xlenUInt
    val valid = in port Bool()
    val write = in port Bool()
    val addr = in port config.xlenUInt
    val strobe = in port Bits(config.nbyte bits)
    val wdata = in port config.xlenBits
    val rdata = out port config.xlenBits
  }

  noIoPrefix()
  mapClockDomain(clock = io.clk, reset = io.rst_b, resetActiveLevel = LOW)
}

