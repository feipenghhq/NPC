/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/14/2023
 *
 * ------------------------------------------------------------------------------------------------
 * core_s: Single Cycle CPU core
 * ------------------------------------------------------------------------------------------------
 */

module core_s #(
    parameter XLEN       = 32,
    parameter PC_RST_VEC = 32'h00000000     // PC reset vector
) (

    input  logic                clk,
    input  logic                rst_b,

    output logic [XLEN-1:0]     pc
);


    // IFU
    IFU #(
          .XLEN(XLEN),
          .PC_RST_VEC(PC_RST_VEC)
         )
    u_IFU (
           .clk(clk),
           .rst_b(rst_b),
           .pc(pc)
          );

endmodule
