import chisel3._
import chisel3.util._

class Accelerator extends Module {
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())

    val address = Output(UInt (16.W))
    val dataRead = Input(UInt (32.W))
    val writeEnable = Output(Bool ())
    val dataWrite = Output(UInt (32.W))

  })

  //State enum and register
  val idle :: borderCheck :: blackCheck :: neighborLeft :: neighborUp :: neighborRight :: neighborDown :: done :: Nil = Enum(8)

  val stateReg = RegInit(idle)

  //Support registers
  val x = RegInit(0.U(16.W))
  val y = RegInit(0.U(16.W))
  val pixelVal = RegInit(0.U(16.W))

  //Default values
  io.address := 0.U(16.W)
  io.writeEnable := false.B
  io.dataWrite := 0.U(16.W)
  io.done := false.B

  //States
  switch(stateReg) {
    is(idle) {
      when(io.start) {
        x := 0.U(16.W)
        y := 0.U(16.W)

        stateReg := borderCheck
      }
    }
    is(borderCheck){
      io.writeEnable := true.B
      when(y === 19.U(16.W) && x != 19.U(16.W)) {
        io.address := x * 20.U(16.W) + y + 400.U(16.W)
        io.dataWrite := 0.U(16.W)

        y := 0.U(16.W)
        x := x + 1.U(16.W)
      }.elsewhen(x === 0.U(16.W) || y === 0.U(16.W) || x === 19.U(16.W) && y != 19.U(16.W)) {
        io.dataWrite := 0.U(16.W)

        y := y + 1.U(16.W)
      }.elsewhen(x === 19.U(16.W) && y === 19.U(16.W)) {
        io.dataWrite := 0.U(16.W)
        stateReg := done
      }.otherwise {
        io.writeEnable := false.B
        io.address := x * 20.U(16.W) + y
        pixelVal := io.dataRead
        stateReg := blackCheck
      }
    }
    is(blackCheck){
      when(pixelVal === 0.U(16.W) ){
        io.address := x * 20.U(16.W) + y + 400.U(16.W)
        io.writeEnable := true.B
        io.dataWrite := 0.U(16.W)

        y := y + 1.U(16.W)
        stateReg := borderCheck
      } otherwise {
        io.address := (x - 1.U(16.W)) * 20.U(16.W) + y
        pixelVal := io.dataRead
        stateReg := neighborLeft
      }
    }
    is(neighborLeft) {
      when (pixelVal === 0.U(16.W)){
        io.address := x * 20.U(16.W) + y + 400.U(16.W)
        io.writeEnable := true.B
        io.dataWrite := 0.U(16.W)

        y := y + 1.U(16.W)
        stateReg := borderCheck
      }.otherwise {
        io.address := x * 20.U(16.W) + (y - 1.U(16.W))
        pixelVal := io.dataRead
        stateReg := neighborUp
      }

    }
    is(neighborUp){
      when(pixelVal === 0.U(16.W)) {
        io.address := x * 20.U(16.W) + y + 400.U(16.W)
        io.writeEnable := true.B
        io.dataWrite := 0.U(16.W)

        y := y + 1.U(16.W)
        stateReg := borderCheck
      }.otherwise {
        io.address := (x + 1.U(16.W)) * 20.U(16.W) + y
        pixelVal := io.dataRead
        stateReg := neighborRight
      }
    }
    is(neighborRight){
      when(pixelVal === 0.U(16.W)) {
        io.address := x * 20.U(16.W) + y + 400.U(16.W)
        io.writeEnable := true.B
        io.dataWrite := 0.U(16.W)

        y := y + 1.U(16.W)
        stateReg := borderCheck
      }.otherwise {
        io.address := x * 20.U(16.W) + (y + 1.U(16.W))
        pixelVal := io.dataRead
        stateReg := neighborDown
      }
    }
    is(neighborDown){
      io.address := x * 20.U(16.W) + y + 400.U(16.W)
      io.writeEnable := true.B

      when(pixelVal === 0.U(16.W)) {
        io.dataWrite := 0.U(16.W)

        y := y + 1.U(16.W)
        stateReg := borderCheck
      }.otherwise {
        io.dataWrite := 255.U(16.W)

        y := y + 1.U(16.W)
        stateReg := borderCheck
      }
    }
    is(done) {
      io.done := true.B
    }
  }

}
