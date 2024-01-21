/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 12/14/2023
 *
 * ------------------------------------------------------------------------------------------------
 * IDU: Instruction Decode Unit
 * ------------------------------------------------------------------------------------------------
 */

`include "riscv_isa.svh"

module IDU #(
    parameter XLEN    = 32,
    parameter REGID_W = 5
) (
    input  logic [XLEN-1:0]    inst,    // input instruction
    // ALU/branch/Mem opcode
    output logic [3:0]         dec_alu_opcode,
    output logic [2:0]         dec_bxx_opcode,
    output logic [2:0]         dec_mem_opcode,
    // alu src1/src2 selection
    output logic               dec_alu_src1_sel_rs1,
    output logic               dec_alu_src1_sel_pc,
    output logic               dec_alu_src1_sel_0,
    output logic               dec_alu_src2_sel_rs2,
    output logic               dec_alu_src2_sel_imm,
    // branch aand jump
    output logic               dec_bxx,
    output logic               dec_jump,
    // memory access
    output logic               dec_mem_read,
    output logic               dec_mem_write,
    // rv32m
    output logic               dec_mul,
    output logic               dec_div,
    // system instruction
    output logic               dec_ebreak,
    output logic               dec_ecall,
    output logic               dec_mret,
    // csr access
    output logic               dec_csr_write,
    output logic               dec_csr_set,
    output logic               dec_csr_clear,
    output logic               dec_csr_read,
    output logic [11:0]        dec_csr_addr,
    output logic               dec_csr_sel_imm,
    // register access
    output logic               dec_rd_write,
    output logic [REGID_W-1:0] dec_rd_addr,
    output logic [REGID_W-1:0] dec_rs1_addr,
    output logic [REGID_W-1:0] dec_rs2_addr,
    // immediate value
    output logic [XLEN-1:0]    dec_imm
);
    // -------------------------------------------
    // Signal definition
    // -------------------------------------------

    // Instruction Field
    logic [1:0] rv32i_phase;
    logic [4:0] rv32i_opcode;
    logic [2:0] rv32i_funct3;
    logic [6:0] rv32i_funct7;

    // immediate value
    logic [XLEN-1:0] u_type_imm_val;
    logic [XLEN-1:0] i_type_imm_val;
    logic [XLEN-1:0] j_type_imm_val;
    logic [XLEN-1:0] s_type_imm_val;
    logic [XLEN-1:0] b_type_imm_val;
    logic [XLEN-1:0] c_type_imm_val;

    // immediate type
    logic u_type_imm;
    logic i_type_imm;
    logic j_type_imm;
    logic s_type_imm;
    logic b_type_imm;
    logic c_type_imm; // for csr

    // phase
    logic phase3;

    // Instruction opcode
    logic is_lui;
    logic is_auipc;
    logic is_jal;
    logic is_jalr;
    logic is_itype;
    logic is_rtype;
    logic is_load;
    logic is_store;
    logic is_bxx;
    logic is_system;

    // Instruction: Logic/Arithematic
    logic is_add;
    logic is_sub;
    logic is_sll;
    logic is_slt;
    logic is_sltu;
    logic is_xor;
    logic is_srl;
    logic is_sra;
    logic is_or;
    logic is_and;

    // Instruction: system
    logic is_ebreak;
    logic is_ecall;
    logic is_mret;

    // CSR
    logic is_csrrw;
    logic is_csrrs;
    logic is_csrrc;
    logic is_csr;

    // Special values for some fields for instruction decode
    // Format: <field>_<value>
    logic rv32i_funct3_0;
    logic rv32i_funct7_0x00;
    logic rv32i_funct7_0x01;
    logic rv32i_funct7_0x18;
    logic rv32i_funct7_0x20;

    // MISC
    logic alu_add;

    // -------------------------------------------
    // Extract Each field from Instruction
    // -------------------------------------------

    // Instruction Phase/Opcode/Funct
    assign rv32i_phase  = inst[`R_PHASE];
    assign rv32i_opcode = inst[`R_OPCODE];
    assign rv32i_funct3 = inst[`R_FUNCT3];
    assign rv32i_funct7 = inst[`R_FUNCT7];

    // register address
    assign dec_rs1_addr = inst[`R_RS1];
    assign dec_rs2_addr = inst[`R_RS2];
    assign dec_rd_addr  = inst[`R_RD];

    // -------------------------------------------
    // Instruction Decode
    // -------------------------------------------

    // Specific Values for decode
    assign rv32i_funct3_0 = (rv32i_funct3 == 3'h0);
    assign rv32i_funct7_0x00 = (rv32i_funct7 == 7'h00);
    assign rv32i_funct7_0x01 = (rv32i_funct7 == 7'h01);
    assign rv32i_funct7_0x18 = (rv32i_funct7 == 7'h18);
    assign rv32i_funct7_0x20 = (rv32i_funct7 == 7'h20);

    // Phase Decode
    assign phase3    = (rv32i_phase == 2'b11);

    // RV32I Base Instruction Set Opcode Decode (Not compressed)
    assign is_lui    = phase3 & (rv32i_opcode == `RV32I_OPCODE_LUI);
    assign is_auipc  = phase3 & (rv32i_opcode == `RV32I_OPCODE_AUIPC);
    assign is_jal    = phase3 & (rv32i_opcode == `RV32I_OPCODE_JAL);
    assign is_jalr   = phase3 & (rv32i_opcode == `RV32I_OPCODE_JALR);
    assign is_load   = phase3 & (rv32i_opcode == `RV32I_OPCODE_LOAD);
    assign is_store  = phase3 & (rv32i_opcode == `RV32I_OPCODE_STORE);
    assign is_itype  = phase3 & (rv32i_opcode == `RV32I_OPCODE_ITYPE);
    assign is_rtype  = phase3 & (rv32i_opcode == `RV32I_OPCODE_RTYPE);
    assign is_bxx    = phase3 & (rv32i_opcode == `RV32I_OPCODE_BRANCH);
    assign is_system = phase3 & (rv32i_opcode == `RV32I_OPCODE_SYSTEM);

    // Logic/Arithematic Funct3 Decode
    assign is_sub  = (rv32i_funct3 == `RV32I_FUNCT3_SUB)  & ((is_rtype & rv32i_funct7_0x20));
    assign is_slt  = (rv32i_funct3 == `RV32I_FUNCT3_SLT)  & ((is_rtype & rv32i_funct7_0x00) | is_itype);
    assign is_sltu = (rv32i_funct3 == `RV32I_FUNCT3_SLTU) & ((is_rtype & rv32i_funct7_0x00) | is_itype);
    assign is_xor  = (rv32i_funct3 == `RV32I_FUNCT3_XOR)  & ((is_rtype & rv32i_funct7_0x00) | is_itype);
    assign is_or   = (rv32i_funct3 == `RV32I_FUNCT3_OR)   & ((is_rtype & rv32i_funct7_0x00) | is_itype);
    assign is_and  = (rv32i_funct3 == `RV32I_FUNCT3_AND)  & ((is_rtype & rv32i_funct7_0x00) | is_itype);
    assign is_add  = (rv32i_funct3 == `RV32I_FUNCT3_ADD)  & ((is_rtype & rv32i_funct7_0x00) | is_itype);
    assign is_sll  = (rv32i_funct3 == `RV32I_FUNCT3_SLL)  & ((is_rtype & rv32i_funct7_0x00) | (is_itype & rv32i_funct7_0x00));
    assign is_srl  = (rv32i_funct3 == `RV32I_FUNCT3_SRL)  & ((is_rtype & rv32i_funct7_0x00) | (is_itype & rv32i_funct7_0x00));
    assign is_sra  = (rv32i_funct3 == `RV32I_FUNCT3_SRA)  & ((is_rtype & rv32i_funct7_0x20) | (is_itype & rv32i_funct7_0x20));

    // ebreak
    assign is_ebreak = is_system & (inst[31:7] == 25'h2000);
    assign is_ecall = is_system & (inst[31:7] == 25'h0);
    assign is_mret = is_system & (inst[31:7] == 25'h604000);

    // -------------------------------------------
    // Control signal generation
    // -------------------------------------------

    // ALU source 1 selection
    // RS1 value : Default
    // PC        : JAL/AUIPC/BRANCH
    // Zero      : LUI
    assign dec_alu_src1_sel_pc = is_jal | is_auipc | is_bxx;
    assign dec_alu_src1_sel_0  = is_lui;
    assign dec_alu_src1_sel_rs1 = ~(dec_alu_src1_sel_pc | dec_alu_src1_sel_0);

    // ALU source 2 selection
    // RS2 value: Default
    // Immediate: JAL/JALR/BRANCH/LUI/AUIPC/ITYPE/LOAD/STORE
    assign dec_alu_src2_sel_imm = is_jal | is_jalr | is_bxx | is_lui | is_auipc | is_itype | is_load | is_store;
    assign dec_alu_src2_sel_rs2 = ~dec_alu_src2_sel_imm;

    // ALU/BRANCH/MEM Opcode is the same as Funct3
    assign dec_bxx_opcode = rv32i_funct3;
    assign dec_mem_opcode = rv32i_funct3;

    // ALU use add operation for jal/jalr/bxx/load/store instruction
    assign alu_add = is_lui | is_auipc | dec_jump | dec_bxx | dec_mem_read | dec_mem_write;
    // ALU opcode
    assign dec_alu_opcode[2:0] = alu_add ? `RV32I_FUNCT3_ADD : rv32i_funct3;
    // additional bit to distingush between add/sub, srli/srai, and srl, sra
    assign dec_alu_opcode[3] = is_sub | is_sra;

    // Memory read/write
    assign dec_mem_read  = is_load;
    assign dec_mem_write = is_store;

    // Jump/Branch
    assign dec_jump = is_jal | is_jalr;
    assign dec_bxx  = is_bxx;

    // system
    assign dec_ebreak = is_ebreak;
    assign dec_ecall = is_ecall;
    assign dec_mret = is_mret;


    // registr write
    assign dec_rd_write = is_lui | is_auipc | dec_jump | is_itype | is_rtype | is_load | is_csr;

    // -------------------------------------------
    // ZICSR Extension
    // -------------------------------------------

    logic csrrw_read;
    logic csrrs_write;
    logic csrrc_write;

    assign is_csrrw = is_system & (rv32i_funct3[1:0] == 2'b01);
    assign is_csrrs = is_system & (rv32i_funct3[1:0] == 2'b10);
    assign is_csrrc = is_system & (rv32i_funct3[1:0] == 2'b11);
    assign is_csr = is_csrrw | is_csrrs | is_csrrc;

    assign csrrw_read  = dec_rd_addr != 0;   // csrrw/csrrwi should not read CSR if rd = x0
    assign csrrs_write = dec_rs1_addr != 0;  // csrrs/csrrsi should not write CSR if rs1 = x0 (uimm = 0)
    assign csrrc_write = csrrs_write;        // csrrc/csrrci should not write CSR if rs1 = x0 (uimm = 0)

    assign dec_csr_write = is_csrrw;
    assign dec_csr_set   = is_csrrs & csrrs_write;
    assign dec_csr_clear = is_csrrc & csrrc_write;
    assign dec_csr_read  = is_csrrw & csrrw_read | is_csrrs | is_csrrc;
    assign dec_csr_addr  = inst[31:20];
    assign dec_csr_sel_imm = is_system & (rv32i_funct3[2]);
    assign c_type_imm    = is_system & rv32i_funct3[2];
    assign c_type_imm_val = {27'b0, dec_rs1_addr};

    // -------------------------------------------
    // RV32M Extension
    // -------------------------------------------
    assign dec_mul = is_rtype & rv32i_funct7_0x01 & ~rv32i_funct3[2];
    assign dec_div = is_rtype & rv32i_funct7_0x01 & rv32i_funct3[2];

    // -------------------------------------------
    // Immediate generation
    // -------------------------------------------

    assign i_type_imm = is_jalr | is_itype | is_load;
    assign u_type_imm = is_lui  | is_auipc;
    assign j_type_imm = is_jal;
    assign s_type_imm = is_store;
    assign b_type_imm = is_bxx;

    assign u_type_imm_val = {inst[31:12], 12'b0};
    assign i_type_imm_val = {{20{inst[31]}}, inst[31:20]};
    assign j_type_imm_val = {{12{inst[31]}}, inst[19:12], inst[20], inst[30:21], 1'b0};
    assign s_type_imm_val = {{20{inst[31]}}, inst[31:25], inst[11:7]};
    assign b_type_imm_val = {{20{inst[31]}}, inst[7], inst[30:25], inst[11:8], 1'b0};

    assign dec_imm = ({XLEN{i_type_imm}} & i_type_imm_val) |
                     ({XLEN{u_type_imm}} & u_type_imm_val) |
                     ({XLEN{j_type_imm}} & j_type_imm_val) |
                     ({XLEN{s_type_imm}} & s_type_imm_val) |
                     ({XLEN{b_type_imm}} & b_type_imm_val) |
                     ({XLEN{c_type_imm}} & c_type_imm_val);
endmodule
