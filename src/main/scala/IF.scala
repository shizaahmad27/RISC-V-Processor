package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class InstructionFetch extends MultiIOModule {

  // Don't touch
  val testHarness = IO(
    new Bundle {
      val IMEMsetup = Input(new IMEMsetupSignals)
      val PC        = Output(UInt())
    }
  )


  val io = IO(
    new Bundle {
      val PC = Output(UInt())
      val instruction = Output(new Instruction)
      val branchTaken = Input(Bool())
      val branchTarget = Input(UInt(32.W))
      val stall = Input(Bool())
    })

  val IMEM = Module(new IMEM)
  val PC   = RegInit(UInt(32.W), 0.U)


  /**
    * Setup. You should not change this code
    */
  IMEM.testHarness.setupSignals := testHarness.IMEMsetup
  testHarness.PC := IMEM.testHarness.requestedAddress



  io.PC := PC
  // On a stall the instruction in ID must be fetched again, since PC already points past it
  IMEM.io.instructionAddress := Mux(io.stall, PC - 4.U, PC)

  when(io.stall){
    PC := PC
  }.elsewhen(io.branchTaken){
    PC := io.branchTarget
  }.otherwise {
    PC := PC + 4.U
  }

  val instruction = Wire(new Instruction)
  instruction := IMEM.io.instruction.asTypeOf(new Instruction)
  io.instruction := instruction

  /**
    * Setup. You should not change this code.
    */
  when(testHarness.IMEMsetup.setup) {
    PC := 0.U
    instruction := Instruction.NOP
  }
}
