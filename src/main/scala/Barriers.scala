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
      val stall = Input(Bool())
    }
  )

  val reg = RegInit(0.U(32.W))
  // Having the registers as a barrier between here will give one cycle delay
  when(!io.stall){
    reg := io.inPC
  }
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

      val inRS2Data = Input(UInt(32.W))
      val outRS2Data = Output(UInt(32.W))
      val inMemRead = Input(Bool())
      val outMemRead = Output(Bool())
      val inMemWrite = Input(Bool())
      val outMemWrite = Output(Bool())

      val inBranch = Input(Bool())
      val outBranch = Output(Bool())
      val inJump = Input(Bool())
      val outJump = Output(Bool())
      val inBranchType = Input(UInt(3.W))
      val outBranchType = Output(UInt(3.W))
      val inImmediate = Input(UInt(32.W))
      val outImmediate = Output(UInt(32.W))
      val inPCout = Input(UInt(32.W))
      val outPCout = Output(UInt(32.W))

      val inRS1 = Input(UInt(5.W))
      val outRS1 = Output(UInt(5.W))

      val inRS2 = Input(UInt(5.W))
      val outRS2 = Output(UInt(5.W))
      val inOp1IsRegister = Input(Bool())
      val outOp1IsRegister = Output(Bool())
      val inOp2IsRegister = Input(Bool())
      val outOp2IsRegister = Output(Bool())

      val stall = Input(Bool())
    }
  )

 val regOp1 = RegInit(0.U(32.W))
 val regOp2 = RegInit(0.U(32.W))
 val regALUOp = RegInit(0.U(4.W))
 val regRS1 = RegInit(0.U(5.W))
 val regRS2 = RegInit(0.U(5.W))
 val regRegWrite = RegInit(false.B)
 val regRegisterRd = RegInit(0.U(5.W))
 val regRS2Data = RegInit(0.U(32.W))
 val regMemRead = RegInit(false.B)
 val regMemWrite = RegInit(false.B)

 val regOp1IsRegister = RegInit(false.B)
 val regOp2IsRegister = RegInit(false.B)
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

 regRS2Data := io.inRS2Data
 regMemRead := io.inMemRead
 regMemWrite := io.inMemWrite
 io.outRS2Data := regRS2Data
 io.outMemRead := regMemRead
 io.outMemWrite := regMemWrite

 val regBranch = RegInit(false.B)
 val regJump = RegInit(false.B)
 val regBranchType = RegInit(0.U(3.W))
 val regImmediate = RegInit(0.U(32.W))
 val regPCout = RegInit(0.U(32.W))

 regBranch := io.inBranch
 regJump := io.inJump
 regBranchType := io.inBranchType
 regImmediate := io.inImmediate
 regPCout := io.inPCout

 io.outBranch := regBranch
 io.outJump := regJump
 io.outBranchType := regBranchType
 io.outImmediate := regImmediate
 io.outPCout := regPCout

 regRS1 := io.inRS1
 regRS2 := io.inRS2
 io.outRS1 := regRS1
 io.outRS2 := regRS2
 io.outRS2Data := regRS2Data
 io.outOp1IsRegister := regOp1IsRegister
 io.outOp2IsRegister := regOp2IsRegister
 regOp1IsRegister := io.inOp1IsRegister
 regOp2IsRegister := io.inOp2IsRegister

 // Load-use stall: insert a bubble into EX
 when(io.stall){
   regRegWrite := false.B
   regMemRead := false.B
   regMemWrite := false.B
   regBranch := false.B
   regJump := false.B
 }
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

      val inRS2Data = Input(UInt(32.W))
      val outRS2Data = Output(UInt(32.W))
      val inMemRead = Input(Bool())
      val outMemRead = Output(Bool())
      val inMemWrite = Input(Bool())
      val outMemWrite = Output(Bool())
    }
  )

  val regALUResult = RegInit(0.U(32.W))
  val regRegWrite = RegInit(false.B)
  val regRegisterRd = RegInit(0.U(5.W))
  val regRS2Data = RegInit(0.U(32.W))
  val regMemRead = RegInit(false.B)
  val regMemWrite = RegInit(false.B)


  regALUResult := io.inALUResult
  regRegWrite := io.inRegWrite
  regRegisterRd := io.inRegisterRd
  regRS2Data := io.inRS2Data
  regMemRead := io.inMemRead
  regMemWrite := io.inMemWrite

  io.outALUResult := regALUResult
  io.outRegWrite := regRegWrite
  io.outRegisterRd := regRegisterRd
  io.outRS2Data := regRS2Data
  io.outMemRead := regMemRead
  io.outMemWrite := regMemWrite
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
      val inMemRead = Input(Bool())
      val outMemRead = Output(Bool())
    }
  )

  val regWriteBackData = RegInit(0.U(32.W))
  val regRegWrite = RegInit(false.B)
  val regRegisterRd = RegInit(0.U(5.W))
  val regMemRead = RegInit(false.B)

  regWriteBackData := io.inWriteBackData
  regRegWrite := io.inRegWrite
  regRegisterRd := io.inRegisterRd
  regMemRead := io.inMemRead

  io.outWriteBackData := regWriteBackData
  io.outRegWrite := regRegWrite
  io.outRegisterRd := regRegisterRd
  io.outMemRead := regMemRead
}