package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class CPU extends MultiIOModule {

  val testHarness = IO(
    new Bundle {
      val setupSignals = Input(new SetupSignals)
      val testReadouts = Output(new TestReadouts)
      val regUpdates   = Output(new RegisterUpdates)
      val memUpdates   = Output(new MemUpdates)
      val currentPC    = Output(UInt(32.W))
    }
  )

  /**
    You need to create the classes for these yourself
    */
   val IFBarrier  = Module(new IFBarrier).io
   val IDBarrier  = Module(new IDBarrier).io
   val EXBarrier  = Module(new EXBarrier).io
   val MEMBarrier = Module(new MEMBarrier).io

  val ID  = Module(new InstructionDecode)
  val IF  = Module(new InstructionFetch)
  val EX  = Module(new Execute)
  val MEM = Module(new MemoryFetch)
  // val WB  = Module(new Execute) (You may not need this one?)


  /**
    * Setup. You should not change this code
    */
  IF.testHarness.IMEMsetup     := testHarness.setupSignals.IMEMsignals
  ID.testHarness.registerSetup := testHarness.setupSignals.registerSignals
  MEM.testHarness.DMEMsetup    := testHarness.setupSignals.DMEMsignals

  testHarness.testReadouts.registerRead := ID.testHarness.registerPeek
  testHarness.testReadouts.DMEMread     := MEM.testHarness.DMEMpeek

  /**
    spying stuff
    */
  testHarness.regUpdates := ID.testHarness.testUpdates
  testHarness.memUpdates := MEM.testHarness.testUpdates
  testHarness.currentPC  := IF.testHarness.PC

//Connecting IF - BARRIER - ID
  IFBarrier.inPC := IF.io.PC
  IFBarrier.inInstruction := IF.io.instruction
  ID.io.instruction :=  IFBarrier.outInstruction

// Connecting ID - BARRIER - EX
  IDBarrier.inOp1 := ID.io.op1
  IDBarrier.inOp2 := ID.io.op2
  IDBarrier.inALUOp := ID.io.aluOp
  IDBarrier.inRegWrite := ID.io.regWrite
  IDBarrier.inRegisterRd := ID.io.registerRd
  EX.io.op1 := IDBarrier.outOp1
  EX.io.op2 := IDBarrier.outOp2
  EX.io.aluOp := IDBarrier.outALUOp
   
  IDBarrier.inRS2Data  := ID.io.rs2Data
  IDBarrier.inMemRead  := ID.io.memRead
  IDBarrier.inMemWrite := ID.io.memWrite


  //Connecting EX - BARRIER - MEM 
  EXBarrier.inALUResult := EX.io.aluResult
  EXBarrier.inRegWrite := IDBarrier.outRegWrite
  EXBarrier.inRegisterRd := IDBarrier.outRegisterRd
  MEM.io.aluResult := EXBarrier.outALUResult
  EXBarrier.inRS2Data := IDBarrier.outRS2Data
  EXBarrier.inMemRead := IDBarrier.outMemRead
  EXBarrier.inMemWrite := IDBarrier.outMemWrite

  // Connecting MEM - BARRIER - WB
  MEMBarrier.inWriteBackData := MEM.io.writeBackData
  MEMBarrier.inRegWrite := EXBarrier.outRegWrite
  MEMBarrier.inRegisterRd := EXBarrier.outRegisterRd
  MEMBarrier.inMemRead := EXBarrier.outMemRead

  ID.io.writeEnable := MEMBarrier.outRegWrite
  ID.io.writeAddress := MEMBarrier.outRegisterRd
  ID.io.writeData := Mux(MEMBarrier.outMemRead, MEM.io.dataOut, MEMBarrier.outWriteBackData)

  MEM.io.rs2Data  := EXBarrier.outRS2Data
  MEM.io.memRead  := EXBarrier.outMemRead
  MEM.io.memWrite := EXBarrier.outMemWrite
}
