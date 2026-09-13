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

class IDBarrier extends MultiIOModule {
  val io = IO(
    new Bundle {
      val inOp1 = Input(UInt(32.W))
      val inOp2 = Input(UInt(32.W))
      val inALUOp = Input(UInt(4.W))
      val outOp1 = Output(UInt(32.W))
      val outOp2 = Output(UInt(32.W))
      val outALUOp = Output(UInt(4.W))
    }
  )
 val regOp1 = RegInit(0.U(32.W))
 val regOp2 = RegInit(0.U(32.W))
 val regALUOp = RegInit(0.U(4.W))

 regOp1 := io.inOp1
 regOp2 := io.inOp2
 regALUOp := io.inALUOp

 io.outOp1 := regOp1
 io.outOp2 := regOp2
 io.outALUOp := regALUOp
}

