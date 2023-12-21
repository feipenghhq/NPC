/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/14/2023
 *
 * ------------------------------------------------------------------------------------------------
 * IFU: Instruction Fetch Unit
 * ------------------------------------------------------------------------------------------------
 */


module IFU #(
    parameter XLEN       = 32,
    parameter PC_RST_VEC = 32'h00000000     // PC reset vector
) (

    input  logic                clk,
    input  logic                rst_b,

    input  logic                pc_branch,
    input  logic [XLEN-1:0]     target_pc,
    output logic [XLEN-1:0]     pc
);

    always @(posedge clk) begin
        if (!rst_b) begin
            pc <= PC_RST_VEC;
        end
        else begin
            if (pc_branch) begin
                pc <= target_pc;
            end
            else begin
                pc <= pc + 4;
            end
        end
    end

endmodule
