package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase, MuxLookup}
import chisel3.experimental.MultiIOModule


class InstructionDecode extends MultiIOModule {

  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val registerSetup = Input(new RegisterSetupSignals)
      val registerPeek  = Output(UInt(32.W))

      val testUpdates   = Output(new RegisterUpdates)
    })


  val io = IO(
    new Bundle {
      val instruction = Input(new Instruction)
      val op1 = Output(UInt(32.W))
      val op2 = Output(UInt(32.W))
      val aluOp = Output(UInt(4.W))
      val regWrite =  Output(Bool())
      val registerRd = Output(UInt(5.W ))

      val writeEnable = Input(Bool())
      val writeAddress = Input(UInt(5.W))
      val writeData = Input(UInt(32.W))
    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io

  val immediate = MuxLookup(decoder.immType, 0.S(32.W), 
    Array( ImmFormat.ITYPE -> io.instruction.immediateIType,  
  ))

  val op1 = registers.io.readData1
  val op2 = Mux(decoder.op2Select === Op2Select.imm, immediate.asUInt(), registers.io.readData2)

  io.op1 := op1
  io.op2 := op2
  io.aluOp := decoder.ALUop
  io.regWrite := decoder.controlSignals.regWrite
  io.registerRd := io.instruction.registerRd
  /**
    * Setup. You should not change this code
    */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates


  registers.io.readAddress1 := io.instruction.registerRs1
  registers.io.readAddress2 := io.instruction.registerRs2
  registers.io.writeEnable  := io.writeEnable
  registers.io.writeAddress := io.writeAddress
  registers.io.writeData    := io.writeData

  decoder.instruction := io.instruction
} 
