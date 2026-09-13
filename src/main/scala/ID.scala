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

      val rs2Data = Output(UInt(32.W))
      val memRead = Output(Bool())
      val memWrite = Output(Bool())
      val PC = Input(UInt(32.W))
      val PCout = Output(UInt(32.W))
      val immediate = Output(UInt(32.W))
      val branchType = Output(UInt(3.W))
      val branch = Output(Bool())
      val jump = Output(Bool())
    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io

  val immediate = MuxLookup(decoder.immType, 0.S(32.W), 
    Array( 
      ImmFormat.ITYPE -> io.instruction.immediateIType,  
      ImmFormat.SHAMT -> io.instruction.immediateSHAMT,
      ImmFormat.STYPE -> io.instruction.immediateSType,
      ImmFormat.UTYPE -> io.instruction.immediateUType,
      ImmFormat.JTYPE -> io.instruction.immediateJType,
      ImmFormat.BTYPE -> io.instruction.immediateBType,
  ))

  val op1 = Mux(decoder.op1Select === Op1Select.rs1, registers.io.readData1, io.PC)
  val op2 = Mux(decoder.op2Select === Op2Select.imm, immediate.asUInt(), registers.io.readData2)

  io.op1 := op1
  io.op2 := op2
  io.aluOp := decoder.ALUop
  io.regWrite := decoder.controlSignals.regWrite
  io.registerRd := io.instruction.registerRd

  io.rs2Data := registers.io.readData2
  io.memRead := decoder.controlSignals.memRead
  io.memWrite := decoder.controlSignals.memWrite
  io.PCout := io.PC
  io.immediate := immediate.asUInt()
  io.branchType := decoder.branchType
  io.branch := decoder.controlSignals.branch
  io.jump := decoder.controlSignals.jump
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
