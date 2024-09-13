import chisel3._
import chisel3.util._

class ProgramCounter extends Module {
  val io = IO(new Bundle {
    val stop = Input(Bool())
    val jump = Input(Bool())
    val run = Input(Bool())
    val programCounterJump = Input(UInt(17.W))
    val programCounter = Output(UInt(16.W))
  })

  //initialize
  val countReg = RegInit(0.U(16.W))

  //Implement this module here (respect the provided interface, since it used by the tester)
  val runRes = io.stop || !io.run
  val jumpRes = Mux(io.jump, io.programCounterJump, countReg + 1.U)
  countReg := Mux(runRes, countReg, jumpRes)

  io.programCounter := countReg

}