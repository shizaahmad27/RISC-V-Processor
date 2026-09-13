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
    
      val inRegWrite = Input(Bool())
      val inRegisterRd = Input(UInt(5.W))
      val outRegWrite = Output(Bool())
      val outRegisterRd = Output(UInt(5.W))
    }
  )

 val regOp1 = RegInit(0.U(32.W))
 val regOp2 = RegInit(0.U(32.W))
 val regALUOp = RegInit(0.U(4.W))

 val regRegWrite = RegInit(false.B)
 val regRegisterRd = RegInit(0.U(5.W))

 regOp1 := io.inOp1
 regOp2 := io.inOp2
 regALUOp := io.inALUOp
 regRegWrite := io.inRegWrite
 regRegisterRd := io.inRegisterRd
 io.outRegWrite := regRegWrite
 io.outRegisterRd := regRegisterRd
 io.outOp1 := regOp1
 io.outOp2 := regOp2
 io.outALUOp := regALUOp
}

class EXBarrier extends MultiIOModule {
  val io = IO(
    new Bundle {
      val inALUResult = Input(UInt(32.W))
      val outALUResult = Output(UInt(32.W))
      val inRegWrite = Input(Bool())
      val outRegWrite = Output(Bool())
      val inRegisterRd = Input(UInt(5.W))
      val outRegisterRd = Output(UInt(5.W))
    
    }
  )

  val regALUResult = RegInit(0.U(32.W))
  val regRegWrite = RegInit(false.B)
  val regRegisterRd = RegInit(0.U(5.W))


  regALUResult := io.inALUResult
  regRegWrite := io.inRegWrite
  regRegisterRd := io.inRegisterRd

  io.outALUResult := regALUResult
  io.outRegWrite := regRegWrite
  io.outRegisterRd := regRegisterRd
}

class MEMBarrier extends MultiIOModule {
  val io = IO(
    new Bundle {
      val inWriteBackData = Input(UInt(32.W))
      val inRegWrite = Input(Bool())
      val inRegisterRd = Input(UInt(5.W))
      val outRegWrite = Output(Bool())
      val outRegisterRd = Output(UInt(5.W))
      val outWriteBackData = Output(UInt(32.W))
    }
  )

  val regWriteBackData = RegInit(0.U(32.W))
  val regRegWrite = RegInit(false.B)
  val regRegisterRd = RegInit(0.U(5.W))

  regWriteBackData := io.inWriteBackData
  regRegWrite := io.inRegWrite
  regRegisterRd := io.inRegisterRd

  io.outWriteBackData := regWriteBackData
  io.outRegWrite := regRegWrite
  io.outRegisterRd := regRegisterRd
}