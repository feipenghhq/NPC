/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/26/2023
 *
 * ------------------------------------------------------------------------------------------------
 * BEU: Calculate branch result
 * ------------------------------------------------------------------------------------------------
 */

module BEU #(
    parameter XLEN    = 32,
    parameter BXXOP_W = 3
) (
    input  logic [BXXOP_W-1:0] bxx_opcode,
    input  logic [XLEN-1:0]    src1,
    input  logic [XLEN-1:0]    src2,
    output logic               result
);

    // --------------------------------------
    //  Signal Definition
    // --------------------------------------

    logic adder_cin;
    logic adder_cout;
    logic [XLEN-1:0] adder_src1;
    logic [XLEN-1:0] adder_src2;
    logic [XLEN-1:0] adder_result;

    logic eq_result;
    logic lt_result;
    logic ltu_result;

    logic is_eq;
    logic is_lt;
    logic is_ltu;
    logic result_b4_inv;
    logic inv;

    // --------------------------------------
    // Calculation
    // --------------------------------------

    // do subtraction as addition
    assign adder_src1 = src1;
    assign adder_src2 = ~src2;
    assign adder_cin = 1'b1;
    assign {adder_cout, adder_result} = adder_src1 + adder_src2 + {{XLEN{1'b0}}, adder_cin};

    // lt result can be calculated with the following cases
    // 1. src1 < 0 and src2 > 0
    // 2. src1 and src2 are both positive or negative, and adder result is negative
    //    a. both src1/src2 > 0 and src1 - src2 < 0
    //    b. both src1/src2 < 0 and src1 - src2 < 0
    assign lt_result = (src1[XLEN-1] & ~src2[XLEN-1]) |
                       (~(src1[XLEN-1] ^ src2[XLEN-1])) & adder_result[XLEN-1];

    // for ltu, if there is no carry, then src1 is smaller then src2
    assign ltu_result = ~adder_cout;

    assign eq_result = (adder_result == 0);

    // --------------------------------------
    // Select the result based on opcode
    // --------------------------------------
    assign is_eq  = bxx_opcode[2:1] == 2'b00;
    assign is_lt  = bxx_opcode[2:1] == 2'b10;
    assign is_ltu = bxx_opcode[2:1] == 2'b11;
    assign inv    = bxx_opcode[0];

    assign result_b4_inv = is_eq  & eq_result |
                           is_lt  & lt_result |
                           is_ltu & ltu_result;

    assign result = inv ? ~result_b4_inv : result_b4_inv;

endmodule



