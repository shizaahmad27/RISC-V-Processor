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

      val rs1 = Output(UInt(5.W))
      val rs2 = Output(UInt(5.W))

      val op1IsRegister = Output(Bool())
      val op2IsRegister = Output(Bool())

      // Fast branch. Forwarding from MEM stage into the branch comparator
      val memAluResult = Input(UInt(32.W))
      val memRegWrite = Input(Bool())
      val memRd = Input(UInt(5.W))

      val branchTaken = Output(Bool())
      val branchTarget = Output(UInt(32.W))
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


  val rs1Value = Mux(io.writeEnable && io.writeAddress === io.instruction.registerRs1 && io.writeAddress =/= 0.U, io.writeData, registers.io.readData1)
  val rs2Value = Mux(io.writeEnable && io.writeAddress === io.instruction.registerRs2 && io.writeAddress =/= 0.U, io.writeData, registers.io.readData2)

  val op1 = Mux(decoder.op1Select === Op1Select.rs1, rs1Value, io.PC)
  val op2 = Mux(decoder.op2Select === Op2Select.imm, immediate.asUInt(), rs2Value)

  io.op1 := op1
  io.op2 := op2
  io.aluOp := decoder.ALUop
  io.regWrite := decoder.controlSignals.regWrite
  io.registerRd := io.instruction.registerRd

  io.rs2Data := rs2Value
  io.memRead := decoder.controlSignals.memRead
  io.memWrite := decoder.controlSignals.memWrite
  io.PCout := io.PC
  io.immediate := immediate.asUInt()
  io.branchType := decoder.branchType
  io.branch := decoder.controlSignals.branch
  io.jump := decoder.controlSignals.jump

  io.rs1 := io.instruction.registerRs1
  io.rs2 := io.instruction.registerRs2

  io.op1IsRegister := decoder.op1Select === Op1Select.rs1
  io.op2IsRegister := decoder.op2Select === Op2Select.rs2

  // Fast branch: branches and jumps are resolved here in ID.
  // rs1Value/rs2Value already bypass from WB, so only MEM forwarding is added.
  // Values still in EX (or a load in MEM) are handled by stalling in CPU.
  val branchRs1 = Mux(io.memRegWrite && io.memRd === io.instruction.registerRs1 && io.memRd =/= 0.U, io.memAluResult, rs1Value)
  val branchRs2 = Mux(io.memRegWrite && io.memRd === io.instruction.registerRs2 && io.memRd =/= 0.U, io.memAluResult, rs2Value)

  val comparison = MuxLookup(decoder.branchType, false.B, Array(
    branchType.beq  -> (branchRs1 === branchRs2),
    branchType.neq  -> (branchRs1 =/= branchRs2),
    branchType.lt   -> (branchRs1.asSInt < branchRs2.asSInt),
    branchType.gte  -> (branchRs1.asSInt >= branchRs2.asSInt),
    branchType.ltu  -> (branchRs1 < branchRs2),
    branchType.gteu -> (branchRs1 >= branchRs2),
  ))

  // JALR jumps to rs1 + imm, branches and JAL jump to PC + imm
  val isJALR = decoder.controlSignals.jump && decoder.op1Select === Op1Select.rs1
  val targetBase = Mux(isJALR, branchRs1, io.PC)

  io.branchTaken  := decoder.controlSignals.jump || (decoder.controlSignals.branch && comparison)
  io.branchTarget := (targetBase + immediate.asUInt()) & "hFFFFFFFE".U

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
