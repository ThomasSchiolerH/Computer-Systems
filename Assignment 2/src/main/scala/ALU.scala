import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt (32.W))
    val b = Input(UInt (32.W))
    val sel = Input(UInt (4.W))
    val compRes = Output(Bool())
    val res = Output(UInt (32.W))
  })

  //initialize
  io.res := 0.U(32.W)
  io.compRes := 0.U(1.W)

  switch(io.sel){
    is("b0011".U) { //LD
      io.res := io.a
    }
    is ("b0001".U){ //Li
      io.res := io.b
    }
    is("b0010".U) { //SUB
      io.res := io.a - io.b
    }
    is("b0100".U) { //ADD
      io.res := io.a + io.b
    }
    is("b1000".U) { //SUBI
      io.res := io.a - io.b
    }
    is("b1001".U) { //ADDI
      io.res := io.a + io.b
    }
    is("b1010".U) { //Mult
      io.res := io.a * io.b
    }
    is("b1011".U) { //JR
      io.compRes := 1.U
    }
    is("b1100".U) { //JLT
      when (io.a < io.b){
        io.compRes := 1.U
      } otherwise {
        io.compRes := 0.U
      }
    }
    is("b1101".U) { //JEQ
      when(io.a === io.b) {
        io.compRes := 1.U
      } otherwise {
        io.compRes := 0.U
      }
    }
    is("b1110".U) { //SD
      io.res := io.b
    }
    is("b1111".U) { //END
      io.res := io.b
    }
  }

}