/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Author: Heqing Huang
 * Date Created: 10/22/2024
 *
 * ------------------------------------------------------------------------------------------------
 * RrArbiter: Round Robin Arbiter
 * ------------------------------------------------------------------------------------------------
 */

package common

import spinal.core._
import spinal.lib._

/**
  * A round robin arbiter
  *
  * @param width number of requester
  */

case class RrArbiter(width: Int) extends Component {
    val io = new Bundle {
        val req = in port Bits(width bit)              // request
        val enable = in port Bool                      // enable the arbiter
        val grant = out port UInt(width bit)           // grant
        val grantId = out port UInt(log2Up(width) bit) // the index of the grant
        val prevGrant = out port UInt(width bit)       // previous grant
    }
    noIoPrefix()

    // Record the previous grant result for the next arbitration
    io.prevGrant := RegNextWhen(io.grant, io.enable) init 1
    // Use the double bit mask technic for arbitration and shift the last grant left by 1
    // to make the next request as highest priority
    val doubleReq = (io.req ## io.req).asUInt
    val base = io.prevGrant.rotateLeft(1)
    val doubleGrant = doubleReq & ~(doubleReq - base)
    io.grant   := doubleGrant(width - 1 downto 0) | doubleGrant(width * 2 -1 downto width)
    io.grantId := OHToUInt(io.grant)
}