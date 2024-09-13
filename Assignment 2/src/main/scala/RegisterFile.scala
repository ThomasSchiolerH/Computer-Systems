import chisel3._
import chisel3.util._

class RegisterFile extends Module {
  val io = IO(new Bundle {
    val aSel = Input(UInt (5.W))
    val bSel = Input(UInt (5.W))
    val writeSel = Input(UInt (5.W))
    val writeData = Input(UInt (32.W))
    val writeE = Input(Bool())
    val a = Output(UInt (32.W))
    val b = Output(UInt (32.W))
  })

  val registerFile = Reg(Vec(32, UInt(32.W)))

  when (io.writeE){
    registerFile(io.writeSel) := io.writeData
  }

  io.a := registerFile(io.aSel)
  io.b := registerFile(io.bSel)

}