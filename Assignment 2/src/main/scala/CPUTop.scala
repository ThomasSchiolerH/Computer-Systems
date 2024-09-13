import chisel3._
import chisel3.util._

class CPUTop extends Module {
  val io = IO(new Bundle {
    val done = Output(Bool ())
    val run = Input(Bool ())
    //This signals are used by the tester for loading and dumping the memory content, do not touch
    val testerDataMemEnable = Input(Bool ())
    val testerDataMemAddress = Input(UInt (16.W))
    val testerDataMemDataRead = Output(UInt (32.W))
    val testerDataMemWriteEnable = Input(Bool ())
    val testerDataMemDataWrite = Input(UInt (32.W))
    //This signals are used by the tester for loading and dumping the memory content, do not touch
    val testerProgMemEnable = Input(Bool ())
    val testerProgMemAddress = Input(UInt (16.W))
    val testerProgMemDataRead = Output(UInt (32.W))
    val testerProgMemWriteEnable = Input(Bool ())
    val testerProgMemDataWrite = Input(UInt (32.W))
  })

  //Creating components
  val programCounter = Module(new ProgramCounter())
  val dataMemory = Module(new DataMemory())
  val programMemory = Module(new ProgramMemory())
  val registerFile = Module(new RegisterFile())
  val controlUnit = Module(new ControlUnit())
  val alu = Module(new ALU())

  //Connecting the modules
  programCounter.io.run := io.run
  programCounter.io.stop := false.B

  //read instruction
  programMemory.io.address := programCounter.io.programCounter
  val instruction = programMemory.io.instructionRead

  //opcode to controlUnit
  controlUnit.io.opcode := instruction(31,28)
  val data = instruction(15,0)

  //register
  registerFile.io.writeE := controlUnit.io.regWrite
  registerFile.io.writeSel := instruction(27,23)

  when(controlUnit.io.toReg){
    registerFile.io.aSel := instruction(22,18)
    registerFile.io.bSel := instruction(17,13)
  } otherwise {
    registerFile.io.aSel := instruction(22,18)
    registerFile.io.bSel := instruction(22,18)
  }
  when(!controlUnit.io.regWrite){
    registerFile.io.aSel := instruction(27, 23)
    registerFile.io.bSel := instruction(22, 18)
  }

  //ALU operation
  alu.io.sel := instruction(31,28)
  alu.io.a := registerFile.io.a
  when(controlUnit.io.ALUSrc){
    alu.io.b := data
  } otherwise {
    alu.io.b := registerFile.io.b
  }

  //Data memory operation
  dataMemory.io.address := alu.io.res
  dataMemory.io.dataWrite := registerFile.io.a
  dataMemory.io.writeEnable := controlUnit.io.memWrite

  //Mux operation
  when(controlUnit.io.memToReg){
    registerFile.io.writeData := dataMemory.io.dataRead
  } otherwise {
    registerFile.io.writeData := alu.io.res
  }


  //Jumping operation
  when(controlUnit.io.branch && alu.io.compRes){
    programCounter.io.jump := true.B
    programCounter.io.programCounterJump := data
  } otherwise {
    programCounter.io.jump := false.B
    programCounter.io.programCounterJump := data
  }


  //start-stop program
  when(!io.run || controlUnit.io.stopCode){
    programCounter.io.run := false.B
    io.done := true.B
  } otherwise {
    io.done := false.B
  }

  //This signals are used by the tester for loading the program to the program memory, do not touch
  programMemory.io.testerAddress := io.testerProgMemAddress
  io.testerProgMemDataRead := programMemory.io.testerDataRead
  programMemory.io.testerDataWrite := io.testerProgMemDataWrite
  programMemory.io.testerEnable := io.testerProgMemEnable
  programMemory.io.testerWriteEnable := io.testerProgMemWriteEnable
  //This signals are used by the tester for loading and dumping the data memory content, do not touch
  dataMemory.io.testerAddress := io.testerDataMemAddress
  io.testerDataMemDataRead := dataMemory.io.testerDataRead
  dataMemory.io.testerDataWrite := io.testerDataMemDataWrite
  dataMemory.io.testerEnable := io.testerDataMemEnable
  dataMemory.io.testerWriteEnable := io.testerDataMemWriteEnable
}