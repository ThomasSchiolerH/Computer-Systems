import chisel3._
import chisel3.iotesters.PeekPokeTester


class aluTester(dut: ALU) extends PeekPokeTester(dut) {
  //ADDI
  poke(dut.io.sel, 9.U(4.W))
  poke(dut.io.a, 9.S(32.W))
  poke(dut.io.b, 10.S(32.W))
  step(1)
  expect(dut.io.res, 19.S(32.W))

  //ADD
  poke(dut.io.sel, 4.U(4.W))
  poke(dut.io.a, 9.S(32.W))
  poke(dut.io.b, 10.S(32.W))
  step(1)
  expect(dut.io.res, 19.S(32.W))

  //SUB
  poke(dut.io.sel, 2.U(4.W))
  poke(dut.io.a, 10.S(32.W))
  poke(dut.io.b, 5.S(32.W))
  step(1)
  expect(dut.io.res, 5.S(32.W))

  //Mult
  poke(dut.io.sel, 10.U(4.W))
  poke(dut.io.a, 2.S(32.W))
  poke(dut.io.b, 2.S(32.W))
  step(1)
  expect(dut.io.res, 4.S(32.W))

  //JR
  poke(dut.io.sel, 11.U(4.W))
  poke(dut.io.a, 9.S(32.W))
  poke(dut.io.b, 10.S(32.W))
  step(1)
  expect(dut.io.compRes, true)

  //JLT
  poke(dut.io.sel, 12.U(4.W))
  poke(dut.io.a, 4.S(32.W))
  poke(dut.io.b, 2.S(32.W))
  step(1)
  expect(dut.io.compRes, false)

  poke(dut.io.a, 2.S(32.W))
  poke(dut.io.b, 5.S(32.W))
  step(1)
  expect(dut.io.compRes, true)

  //JEQ
  poke(dut.io.sel, 13.U(4.W))
  poke(dut.io.a, 4.S(32.W))
  poke(dut.io.b, 2.S(32.W))
  step(1)
  expect(dut.io.compRes, false)

  poke(dut.io.a, 5.S(32.W))
  poke(dut.io.b, 5.S(32.W))
  step(1)
  expect(dut.io.compRes, true)

  //SUBBI
  poke(dut.io.sel, 8.U(4.W))
  poke(dut.io.a, 4.S(32.W))
  poke(dut.io.b, 2.S(32.W))
  step(1)
  expect(dut.io.res, 2.S(32.W))

}

object aluTester {
  def main(args: Array[String]): Unit = {
    println("ALU test")
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on",
        "--target-dir", "generated",
        "--top-name", "ALU"),
      () => new ALU()) {
      c => new aluTester(c)
    }
  }
}
