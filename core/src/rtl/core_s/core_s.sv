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
    logic [3:0]         dec_alu_opcode;
    logic [2:0]         dec_bxx_opcode;
    logic [2:0]         dec_mem_opcode;
    logic               dec_alu_src1_sel_rs1;
    logic               dec_alu_src1_sel_pc;
    logic               dec_alu_src1_sel_0;
    logic               dec_alu_src2_sel_rs2;
    logic               dec_alu_src2_sel_imm;
    logic               dec_bxx;
    logic               dec_jump;
    logic               dec_mem_read;
    logic               dec_mem_write;
    logic               dec_mul;
    logic               dec_div;
    logic               dec_ebreak;
    logic               dec_ecall;
    logic               dec_mret;
    logic               dec_rd_write;
    logic [REGID_W-1:0] dec_rd_addr;
    logic [REGID_W-1:0] dec_rs1_addr;
    logic [REGID_W-1:0] dec_rs2_addr;
    logic [XLEN-1:0]    dec_imm;
    logic               dec_csr_write;
    logic               dec_csr_set;
    logic               dec_csr_clear;
    logic               dec_csr_read;
    logic [11:0]        dec_csr_addr;
    logic               dec_csr_sel_imm;

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

    // From MUL/DIV
    logic [XLEN-1:0]    mul_result;
    logic               mul_valid;
    logic [XLEN-1:0]    div_result;
    logic               div_valid;

    // MISC
    logic [XLEN-1:0]    rd_wdata;

    // CSR and Trap
    logic [XLEN-1:0]    csr_wdata;
    logic [XLEN-1:0]    csr_rdata;
    logic               ent_trap;
    logic               trap;
    logic [XLEN-1:0]    trap_pc;

    // CSR register
    logic [XLEN-1:0]    csr_wr_mepc_mepc;
    logic [XLEN-2:0]    csr_wr_mcause_exception_code;
    logic               csr_wr_mcause_interrupt;
    logic [XLEN-3:0]    csr_rd_mtvec_base;
    logic [1:0]         csr_rd_mtvec_mode;
    logic [XLEN-1:0]    csr_rd_mepc_mepc;

    // Memory
    logic [XLEN-1:0]    inst/*verilator public*/;       // input instruction
    logic               data_valid;
    logic               data_wen;
    logic [XLEN-1:0]    data_addr;
    logic [3:0]         data_wstrb;
    logic [XLEN-1:0]    data_wdata;
    logic [XLEN-1:0]    data_rdata;

    // -------------------------------------------
    // Glue logic
    // -------------------------------------------

    // Memory control
    assign data_addr = alu_result;

    // Select which source goes to rd
    assign rd_wdata = dec_mem_read ? mem_rd_wdata :
                      dec_csr_read ? csr_rdata :
                      exu_rd_wdata;
    assign csr_wdata = dec_csr_sel_imm ? dec_imm : rs1_rdata;

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
        .trap(trap),
        .trap_pc(trap_pc),
        .pc(pc));

    IDU #(
        .XLEN(XLEN),
        .REGID_W(REGID_W))
    u_IDU (.*);

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

    EXU #(
        .XLEN(XLEN))
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
         .XLEN(XLEN))
    u_MEU (
        .mem_opcode(dec_mem_opcode),
        .mem_read(dec_mem_read),
        .mem_write(dec_mem_write),
        .byte_addr(alu_result[1:0]),
        .rs2_rdata(rs2_rdata),
        .rd_wdata(mem_rd_wdata),
        .data_valid(data_valid),
        .data_wen(data_wen),
        .data_wstrb(data_wstrb),
        .data_wdata(data_wdata),
        .data_rdata(data_rdata));

    CSR #(
        .XLEN(XLEN))
    u_CSR (
        .csr_write(dec_csr_write),
        .csr_set(dec_csr_set),
        .csr_clear(dec_csr_clear),
        .csr_read(dec_csr_read),
        .csr_wdata(csr_wdata),
        .csr_addr(dec_csr_addr),
        .csr_rdata(csr_rdata),
        .ent_trap(ent_trap),
        .*);

    TRAP #(
       .XLEN(XLEN))
    u_TRAP(
        .ecall(dec_ecall),
        .mret(dec_mret),
        .pc(pc),
        .trap(trap),
        .trap_pc(trap_pc),
        .ent_trap(ent_trap),
        .*);

    MUL #(
       .XLEN(XLEN),
       .ARCH(0))
    u_MUL(
        .clk(clk),
        .rst_b(rst_b),
        .in_valid(dec_mul),
        .in_ready(),
        .opcode(dec_alu_opcode[1:0]), // reuse the alu_opcode
        .op1(rs1_rdata),
        .op2(rs2_rdata),
        .out_valid(mul_valid),
        .result(mul_result));

    DIV #(
       .XLEN(XLEN),
       .ARCH(0))
    u_DIV(
        .clk(clk),
        .rst_b(rst_b),
        .in_valid(dec_div),
        .in_ready(),
        .opcode(dec_alu_opcode[1:0]), // reuse the alu_opcode
        .op1(rs1_rdata),
        .op2(rs2_rdata),
        .out_valid(div_valid),
        .result(div_result));

    // -------------------------------------------
    // _Verilator DPI
    // -------------------------------------------
    `ifdef VERILATOR

        import "DPI-C" function void dpi_set_ebreak();
        import "DPI-C" function void dpi_pmem_read(input int pc, input int addr, output int rdata, input bit ifetch);
        import "DPI-C" function void dpi_pmem_write(input int pc, input int addr, input int data, input byte strb);

        // set ebreak
        always @(posedge clk) begin
            if (dec_ebreak) begin
                dpi_set_ebreak();
            end
        end

        // fetch instruction
        always @(*) begin
            inst = 0;
            if (rst_b) dpi_pmem_read(pc, pc, inst, 1'b1);
        end

        // data memory write, we should only invoke the DPI function at the end of the clock
        // this is because once we call the function, the value in the C/C++ variable will change
        // immediately, but the data should really be written to memory at the end of the clock
        always @(posedge clk) begin
            if (data_valid) begin
                if (data_wen) begin
                    dpi_pmem_write(pc, data_addr, data_wdata, {4'b0, data_wstrb});
                end
            end
        end

        // data memory read, read takes 0 cycle for single cycle core we should read combinationally
        // Another thing is we want to make only ONE call to dpi_pmem_read otherwise we will mess up
        // with the memory trace function (same memory read will be traced multiple time)
        // NOTE: We need to make sure that in verilator the new cycle start with clock being high!!!
        always @(clk) begin
            data_rdata = 0;
            if (!clk && data_valid && !data_wen) begin
                dpi_pmem_read(pc, data_addr, data_rdata, 1'b0);
            end
        end
    `endif


endmodule

