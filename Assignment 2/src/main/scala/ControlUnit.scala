import chisel3._
import chisel3.util._

class ControlUnit extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(4.W))
    val regWrite = Output(Bool())
    val ALUSrc = Output(Bool())
    val ALUCode = Output(Bool())
    val memWrite = Output(Bool())
    val memToReg = Output(Bool())
    val stopCode = Output(Bool())
    val toReg = Output(Bool())
    val branch = Output(Bool())
  })



  //default all outputs to false
  io.regWrite := 0.U(1.W)
  io.ALUSrc := 0.U(1.W)
  io.ALUCode := 0.U(1.W)
  io.memWrite := 0.U(1.W)
  io.memToReg := 0.U(1.W)
  io.stopCode := 0.U(1.W)
  io.toReg := 0.U(1.W)
  io.branch := 0.U(1.W)

  switch(io.opcode){
    is("b0001".U) { //Li
      io.regWrite := 1.U(1.W)
      io.ALUSrc := 1.U(1.W)
    }
    is("b0010".U) { //SUB
      io.regWrite := 1.U(1.W)
      io.toReg := 1.U(1.W)
    }
    is("b0011".U) { //LD
      io.regWrite := 1.U(1.W)
      io.memToReg := 1.U(1.W)
    }
    is("b0100".U) { //ADD
      io.regWrite := 1.U(1.W)
      io.toReg := 1.U(1.W)
    }
    is("b1000".U) { //SUBI
      io.ALUSrc := 1.U(1.W)
      io.regWrite := 1.U(1.W)
    }
    is("b1001".U) { //ADDI
      io.regWrite := 1.U(1.W)
      io.ALUSrc := 1.U(1.W)
    }
    is("b1010".U) { //MULT
      io.regWrite := 1.U(1.W)
      io.toReg := 1.U(1.W)
    }
    is("b1011".U) { //JR
      io.branch := 1.U(1.W)
    }
    is("b1100".U) { //JLT
      io.branch := 1.U(1.W)
    }
    is("b1101".U) { //JEQ
      io.branch := 1.U(1.W)
    }
    is("b1110".U) { //SD
      io.memWrite := 1.U(1.W)
    }
    is("b1111".U) { //END
      io.stopCode := 1.U(1.W)
    }
  }

}