import chisel3._
import chisel3.iotesters.PeekPokeTester

class RegisterFileTester(dut: RegisterFile) extends PeekPokeTester(dut) {

  poke(dut.io.writeE, true)
  poke(dut.io.writeData, 9.S(32.W))
  poke(dut.io.writeSel, 19.U(5.W))
  step(1)
  poke(dut.io.writeData, 0.S(32.W))
  poke(dut.io.aSel, 19.U(5.W))
  step(1)
  expect(dut.io.a, 9.S(32.W))
  step(1)
}

object RegisterFileTester {
  def main(args: Array[String]): Unit = {
    println("Register File test")
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on",
        "--target-dir", "generated",
        "--top-name", "RegisterFile"),
      () => new RegisterFile()) {
      c => new RegisterFileTester(c)
    }
  }
}
