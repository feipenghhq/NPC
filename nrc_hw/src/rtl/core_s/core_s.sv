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
    parameter PC_RST_VEC = 32'h80000000,    // PC reset vector
    parameter REGID_W    = 5                // Register ID width
) (

    input  logic                clk,
    input  logic                rst_b,

    output logic [XLEN-1:0]     pc
);

    // -------------------------------------------
    // localparam definition
    // -------------------------------------------

    localparam ALUOP_W = 4;
    localparam BXXOP_W = 3;
    localparam MEMOP_W = 3;
    localparam R0_ZERO = 1;

    // -------------------------------------------
    // Signal definition
    // -------------------------------------------
    // From IDU
    logic [ALUOP_W-1:0] dec_alu_opcode;
    logic [BXXOP_W-1:0] dec_bxx_opcode;
    logic [MEMOP_W-1:0] dec_mem_opcode;
    logic               dec_alu_src1_sel_rs1;
    logic               dec_alu_src1_sel_pc;
    logic               dec_alu_src1_sel_0;
    logic               dec_alu_src2_sel_rs2;
    logic               dec_alu_src2_sel_imm;
    logic               dec_bxx;
    logic               dec_jump;
    logic               dec_mem_read;
    logic               dec_mem_write;
    logic               dec_ebreak;
    logic               dec_rd_write;
    logic [REGID_W-1:0] dec_rd_addr;
    logic [REGID_W-1:0] dec_rs1_addr;
    logic [REGID_W-1:0] dec_rs2_addr;
    logic [XLEN-1:0]    dec_imm;
    // From RegFile
    logic [XLEN-1:0]    rs1_rdata;
    logic [XLEN-1:0]    rs2_rdata;
    // From EXU
    logic [XLEN-1:0]    exu_rd_wdata;
    logic               pc_branch;
    logic [XLEN-1:0]    target_pc;
    logic [XLEN-1:0]    alu_result;
    // From MEU
    logic [XLEN-1:0]    mem_rd_wdata;
    // MISC
    logic [XLEN-1:0]    rd_wdata;

    // Memory
    logic [XLEN-1:0]    inst;       // input instruction
    logic               data_valid; // data memory request
    logic               data_wen;   // data memory write enable
    logic [XLEN-1:0]    data_addr;  // data memory address
    logic [3:0]         data_wstrb; // data memory write strobe
    logic [XLEN-1:0]    data_wdata; // data memory write data
    logic [XLEN-1:0]    data_rdata; // data memory read data

    // -------------------------------------------
    // Glue logic
    // -------------------------------------------

    // Memory control
    assign data_addr = alu_result;
    assign data_wdata = rs2_rdata;

    // Select which source goes to rd
    assign rd_wdata = dec_mem_read ? mem_rd_wdata : exu_rd_wdata;

    // -------------------------------------------
    // Module Instantiation
    // -------------------------------------------

    // IFU
    IFU #(
        .XLEN(XLEN),
        .PC_RST_VEC(PC_RST_VEC))
    u_IFU (
        .clk(clk),
        .rst_b(rst_b),
        .pc_branch(pc_branch),
        .target_pc(target_pc),
        .pc(pc));

    // IDU
    IDU #(
        .XLEN(XLEN),
        .ALUOP_W(ALUOP_W),
        .BXXOP_W(BXXOP_W),
        .MEMOP_W(MEMOP_W),
        .REGID_W(REGID_W))
    u_IDU (.*);

    // RegFile
    RegFile #(
        .XLEN(XLEN),
        .REGID_W(REGID_W),
        .R0_ZERO(R0_ZERO))
    u_RegFile (
        .clk(clk),
        .rs1_addr(dec_rs1_addr),
        .rs1_rdata(rs1_rdata),
        .rs2_addr(dec_rs2_addr),
        .rs2_rdata(rs2_rdata),
        .rd_addr(dec_rd_addr),
        .rd_wdata(rd_wdata),
        .rd_write(dec_rd_write));

    // EXU
    EXU #(
        .XLEN(XLEN),
        .ALUOP_W(ALUOP_W),
        .BXXOP_W(BXXOP_W),
        .MEMOP_W(MEMOP_W))
    u_EXU (
        .alu_opcode(dec_alu_opcode),
        .bxx_opcode(dec_bxx_opcode),
        .alu_src1_sel_rs1(dec_alu_src1_sel_rs1),
        .alu_src1_sel_pc(dec_alu_src1_sel_pc),
        .alu_src1_sel_0(dec_alu_src1_sel_0),
        .alu_src2_sel_rs2(dec_alu_src2_sel_rs2),
        .alu_src2_sel_imm(dec_alu_src2_sel_imm),
        .pc(pc),
        .rs1_rdata(rs1_rdata),
        .rs2_rdata(rs2_rdata),
        .imm(dec_imm),
        .bxx(dec_bxx),
        .jump(dec_jump),
        .pc_branch(pc_branch),
        .target_pc(target_pc),
        .rd_wdata(exu_rd_wdata),
        .alu_result(alu_result));

    // MEU
    MEU #(
         .XLEN(XLEN),
         .MEMOP_W(MEMOP_W))
    u_MEU (
        .mem_opcode(dec_mem_opcode),
        .mem_read(dec_mem_read),
        .mem_write(dec_mem_write),
        .byte_addr(alu_result[1:0]),
        .rd_wdata(mem_rd_wdata),
        .data_valid(data_valid),
        .data_wen(data_wen),
        .data_wstrb(data_wstrb),
        .data_rdata(data_rdata));

    // -------------------------------------------
    // _Verilator DPI
    // -------------------------------------------
    `ifdef VERILATOR

        import "DPI-C" function void dpi_set_ebreak();
        import "DPI-C" function void dpi_pmem_read(input int addr, output int rdata);
        import "DPI-C" function void dpi_pmem_write(input int addr, input int data, input byte strb);

        // set ebreak
        always @(posedge clk) begin
            if (dec_ebreak) begin
                dpi_set_ebreak();
            end
        end

        // fetch instruction
        always @(*) begin
            dpi_pmem_read(pc, inst);
        end

        // data memory access
        always @(*) begin
            if (data_valid) begin
                dpi_pmem_read(data_addr, data_rdata);
                if (data_wen) begin
                    dpi_pmem_write(data_addr, data_wdata, {4'b0, data_wstrb});
                end
            end
            else begin
                data_rdata = 0;
            end
        end

    `endif


endmodule

