package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class IFBarrier extends MultiIOModule {
  val io = IO(
    new Bundle {
      val inInstruction = Input(new Instruction)
      val outInstruction = Output(new Instruction)
      val outPC = Output(UInt(32.W))
      val inPC = Input(UInt(32.W))
    }
  )

  val reg = RegInit(0.U(32.W))
  // Having the registers as a barrier between here will give one cycle delay
  reg := io.inPC
  io.outPC := reg

  io.outInstruction :=  io.inInstruction

}


